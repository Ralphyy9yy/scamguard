package com.example.scamguard.ui.scan

import android.Manifest
import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.CameraSelector
import androidx.camera.core.ExperimentalGetImage
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.example.scamguard.MainActivity
import com.example.scamguard.R
import com.example.scamguard.databinding.FragmentQrScanBinding
import com.example.scamguard.util.ScamVerdict
import com.example.scamguard.viewmodel.ScanViewModel
import com.google.android.material.snackbar.Snackbar
import com.google.mlkit.vision.barcode.BarcodeScannerOptions
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.barcode.common.Barcode
import com.google.mlkit.vision.common.InputImage
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

class QrScanFragment : Fragment() {

    private var _binding: FragmentQrScanBinding? = null
    private val binding get() = _binding!!

    private val scanViewModel: ScanViewModel by activityViewModels {
        (requireActivity() as MainActivity).viewModelFactory
    }

    private val permissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
            val root = _binding ?: return@registerForActivityResult
            if (granted) {
                startCamera()
            } else {
                Snackbar.make(root.root, "Camera permission is required", Snackbar.LENGTH_LONG).show()
            }
        }

    private var cameraProvider: ProcessCameraProvider? = null
    private lateinit var cameraExecutor: ExecutorService
    private val barcodeScanner by lazy {
        val options = BarcodeScannerOptions.Builder()
            .setBarcodeFormats(Barcode.FORMAT_QR_CODE, Barcode.FORMAT_AZTEC, Barcode.FORMAT_DATA_MATRIX)
            .build()
        BarcodeScanning.getClient(options)
    }
    private var lastPayload: String? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentQrScanBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        cameraExecutor = Executors.newSingleThreadExecutor()

        binding.scanLinkButton.setOnClickListener {
            val text = binding.manualLinkInput.text?.toString().orEmpty()
            if (text.isNotBlank()) {
                scanViewModel.scanLink(text)
            } else {
                Snackbar.make(binding.root, "Enter a link to scan", Snackbar.LENGTH_SHORT).show()
            }
        }

        observeViewModel()
        ensurePermissionAndStartCamera()
    }

    private fun observeViewModel() {
        scanViewModel.scanResult.observe(viewLifecycleOwner) { result ->
            if (result == null || result.type != "qr") {
                binding.qrResultCard.isVisible = false
                return@observe
            }

            binding.qrResultCard.isVisible = true
            binding.qrResultTitle.text = result.verdict.displayName
            binding.qrResultReason.text = result.reason
            binding.qrResultContent.text = result.content

            val colorRes = when (result.verdict) {
                ScamVerdict.SAFE -> R.color.status_safe
                ScamVerdict.SUSPICIOUS -> R.color.status_suspicious
                ScamVerdict.DANGER -> R.color.status_danger
            }
            binding.qrResultTitle.setTextColor(ContextCompat.getColor(requireContext(), colorRes))
        }

        scanViewModel.statusMessage.observe(viewLifecycleOwner) { message ->
            if (!message.isNullOrBlank()) {
                Snackbar.make(binding.root, message, Snackbar.LENGTH_SHORT).show()
                scanViewModel.clearStatusMessage()
            }
        }
    }

    private fun ensurePermissionAndStartCamera() {
        val context = context ?: return
        val granted = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.CAMERA
        ) == PackageManager.PERMISSION_GRANTED

        if (granted) {
            startCamera()
        } else {
            permissionLauncher.launch(Manifest.permission.CAMERA)
        }
    }

    private fun startCamera() {
        val context = context ?: return
        val cameraProviderFuture = ProcessCameraProvider.getInstance(context)
        cameraProviderFuture.addListener({
            cameraProvider = cameraProviderFuture.get()
            bindUseCases()
        }, ContextCompat.getMainExecutor(context))
    }

    private fun bindUseCases() {
        val provider = cameraProvider ?: return
        provider.unbindAll()

        val preview = Preview.Builder().build().also {
            it.setSurfaceProvider(binding.previewView.surfaceProvider)
        }

        val analysis = ImageAnalysis.Builder()
            .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
            .build()
            .also {
                it.setAnalyzer(cameraExecutor, ::processImage)
            }

        provider.bindToLifecycle(
            viewLifecycleOwner,
            CameraSelector.DEFAULT_BACK_CAMERA,
            preview,
            analysis
        )
    }

    @SuppressLint("UnsafeOptInUsageError")
    @OptIn(ExperimentalGetImage::class)
    private fun processImage(imageProxy: ImageProxy) {
        val mediaImage = imageProxy.image
        if (mediaImage == null) {
            imageProxy.close()
            return
        }

        val inputImage = InputImage.fromMediaImage(
            mediaImage,
            imageProxy.imageInfo.rotationDegrees
        )

        barcodeScanner.process(inputImage)
            .addOnSuccessListener { barcodes ->
                val url = barcodes.firstOrNull { barcode ->
                    barcode.valueType == Barcode.TYPE_URL ||
                            barcode.rawValue?.startsWith("http") == true
                }?.rawValue

                if (!url.isNullOrBlank() && url != lastPayload) {
                    lastPayload = url
                    scanViewModel.scanQrPayload(url)
                }
            }
            .addOnFailureListener {
                // Swallow errors and let user try again
            }
            .addOnCompleteListener {
                imageProxy.close()
            }
    }

    override fun onDestroyView() {
        cameraProvider?.unbindAll()
        cameraExecutor.shutdown()
        barcodeScanner.close()
        _binding = null
        super.onDestroyView()
    }
}

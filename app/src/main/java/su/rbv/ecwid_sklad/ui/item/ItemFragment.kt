package su.rbv.ecwid_sklad.ui.item

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.core.content.res.ResourcesCompat
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.bumptech.glide.Glide
import kotlinx.coroutines.launch
import su.rbv.ecwid_sklad.*
import su.rbv.ecwid_sklad.databinding.FragmentItemBinding
import su.rbv.ecwid_sklad.ui.custom.SelectItemBox
import su.rbv.ecwid_sklad.utils.DecimalDigitsInputFilter
import su.rbv.ecwid_sklad.utils.confirmDialog
import su.rbv.ecwid_sklad.utils.getSelectedFile
import su.rbv.ecwid_sklad.utils.toast
import java.io.File
import java.io.IOException

class ItemFragment: Fragment(), SelectItemBox.ISelectItemBoxListener {

    private lateinit var tempPhotoPath: String

    private val viewModel by lazy {
        ViewModelProvider(this,
            ViewModelFactory(
                arguments?.getLong(ARG_ITEM_ID)
                    ?: Const.NEW_ITEM_ID
            )
        ).get(ItemViewModel::class.java)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val binding: FragmentItemBinding = DataBindingUtil.inflate(inflater,
            R.layout.fragment_item, container, false)
        binding.lifecycleOwner = this

        binding.viewModel = viewModel

        /** observe exit event */
        viewModel.isExit.observe(viewLifecycleOwner, Observer {
            if (it) exit()
        })

        binding.itemPrice.filters += DecimalDigitsInputFilter()

        /** observe onDelete item click event */
        binding.itemDelete.setOnClickListener {
            confirmDialog(
                requireActivity(),
                R.string.delete_item_message,
                R.string.button_ok,
                R.string.button_cancel,
                {
                    lifecycleScope.launch {
                        viewModel.deleteItem()
                        exit()
                    }
                }).show()
        }

        /** observe onCancel item click event */
        binding.itemCancel.setOnClickListener {
            exit()
        }

        /** show and create dialog with image actions */
        binding.itemImage.setOnClickListener {
            SelectItemBox.create(
                this,
                arrayListOf(
                    getString(R.string.item_image_photo),
                    getString(R.string.item_image_select),
                    getString(R.string.item_image_delete)
                )
            )
        }

        /** observe error messages */
        viewModel.errorMessage.observe(viewLifecycleOwner, Observer {
            toast(it.data!!)
        })

        /** observe image changes, draw placeholder or image */
        viewModel.itemImage.observe(viewLifecycleOwner, Observer {
            if (it == null) {
                Glide.with(this)
                    .load(ResourcesCompat.getDrawable(requireContext().resources,
                        R.drawable.pic_empty_image, requireContext().theme))
                    .into(binding.itemImage)
            } else {
                Glide.with(this).load(it).into(binding.itemImage)
            }
        })

        return binding.root
    }

    /** image actions listener */
    override fun onItemSelect(position: Int) {
        when (position) {
            ITEM_IMAGE_DELETE -> {
                confirmDialog(
                    requireActivity(),
                    R.string.item_image_delete_message,
                    R.string.button_ok,
                    R.string.button_cancel,
                    {
                        viewModel.setItemImage(null)
                    }).show()
            }
            ITEM_IMAGE_SELECT_PICTURE -> {
                prepareSelectPhoto()
            }
            ITEM_IMAGE_TAKE_PHOTO -> {
                prepareTakePhoto()
            }
        }
    }

    /**
     *  checking and requiring permissions to take photo and write it to disk or take photo immediately
     */
    private fun prepareTakePhoto() {
        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED ||
            ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.CAMERA),
                REQUEST_TAKE_PHOTO
            )
        } else {
            takePhoto()
        }
    }

    /**  handle permission request */
    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (grantResults.isNotEmpty()) {
            for (result in grantResults) {
                if (result != PackageManager.PERMISSION_GRANTED) return
            }
            when (requestCode) {
                REQUEST_TAKE_PHOTO -> {
                    takePhoto()
                }
                REQUEST_SELECT_PHOTO -> {
                    selectPhoto()
                }
            }
        }
    }

    /**
     * on click "select photo" event check permissions and select photo if permissions granted
     */
    private fun prepareSelectPhoto() {
        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE),
                REQUEST_SELECT_PHOTO
            )
        } else {
            selectPhoto()
        }
    }

    /** Start activity with native gallery to select picture */
    private fun selectPhoto() {
        val intent = Intent()
        intent.type = "image/*"
        intent.action = Intent.ACTION_GET_CONTENT
        startActivityForResult(Intent.createChooser(intent, resources.getString(R.string.item_image_photo_select_title)),
            REQUEST_SELECT_PHOTO
        )
    }


    /** prepare and take photo by standard camera */
    private fun takePhoto() {
        val takePictureIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        if (takePictureIntent.resolveActivity(requireActivity().packageManager) != null) {
            val photoFile=  try {
                val storageDir = requireActivity().getExternalFilesDir(Environment.DIRECTORY_PICTURES)
                File.createTempFile(
                    TEMP_PHOTO_FILE_NAME,
                    TEMP_PHOTO_FILE_EXT, storageDir)
            } catch (ex: IOException) {
                toast(R.string.item_image_photo_error)
                return
            }
            tempPhotoPath = photoFile.absolutePath
            val photoURI = FileProvider.getUriForFile(requireContext(),
                FILE_PROVIDER, photoFile)
            takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI)
            startActivityForResult(takePictureIntent,
                REQUEST_TAKE_PHOTO
            )
        }
    }

    /** handle selecting or taking photo result */
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK) {
            when (requestCode) {
                REQUEST_TAKE_PHOTO -> {
                    viewModel.setItemImage(tempPhotoPath)
                }
                REQUEST_SELECT_PHOTO -> {
                    if (data != null && data.data != null) {
                        try {
                            viewModel.setItemImage(
                                getSelectedFile(
                                    requireActivity().contentResolver,
                                    data.data!!
                                )
                            )
                        } catch (e: NullPointerException) {
                            toast(R.string.error_select_file)
                        }
                    }
                }
            }
        }
    }

    /** close current fragment */
    private fun exit() {
        App.cicerone.router.exit()
    }


    companion object {

        private const val FILE_PROVIDER = "su.rbv.ecwid_sklad.android.provider"

        private const val ARG_ITEM_ID = "item_id"

        /** positions into image actions list */
        private const val ITEM_IMAGE_TAKE_PHOTO = 0
        private const val ITEM_IMAGE_SELECT_PICTURE = 1
        private const val ITEM_IMAGE_DELETE = 2

        private const val REQUEST_TAKE_PHOTO = 1001
        private const val REQUEST_SELECT_PHOTO = 1002
        private const val TEMP_PHOTO_FILE_NAME = "temp_photo"
        private const val TEMP_PHOTO_FILE_EXT = ".jpg"

        fun create(itemId: Long): ItemFragment =
            ItemFragment().apply {
                arguments = Bundle().apply {
                    putSerializable(ARG_ITEM_ID, itemId)
                }
            }
    }

}

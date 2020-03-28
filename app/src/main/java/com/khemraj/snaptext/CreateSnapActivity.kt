package com.khemraj.snaptext

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.View
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import com.google.firebase.storage.FirebaseStorage
import java.io.ByteArrayOutputStream
import java.util.*

class CreateSnapActivity : AppCompatActivity() {

    var createSnapImageView :ImageView? = null
    var messageEditText : EditText? = null
    val imagename = UUID.randomUUID().toString() + ".jpg"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_create_snap)
        setTitle("Create Snap")

        createSnapImageView = findViewById(R.id.createSnapImageView)
        messageEditText = findViewById(R.id.messageEditText)
    }

    fun getPhoto() {
        val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        startActivityForResult(intent, 1)
    }

    fun chooseimageClicked(view : View){

        if (checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE), 1)
        } else {
            getPhoto()
        }

    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        val selectedImage = data?.data

        if (requestCode == 1 && resultCode == Activity.RESULT_OK && data != null) {
            try {

                val bitmap = MediaStore.Images.Media.getBitmap(this.contentResolver, selectedImage)
                createSnapImageView?.setImageBitmap(bitmap)

            } catch (e: Exception) {
                e.printStackTrace()
            }

        }

    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == 1) {
            if (grantResults.size > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                getPhoto()
            }
        }
    }

    fun nextClicked(view : View) {

      try {
            // Get the data from an ImageView as bytes
            createSnapImageView?.isDrawingCacheEnabled = true
            createSnapImageView?.buildDrawingCache()
            val bitmap = (createSnapImageView?.drawable as BitmapDrawable).bitmap
            val baos = ByteArrayOutputStream()
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos)
            val data = baos.toByteArray()

            var uploadTask = FirebaseStorage.getInstance().getReference().child("image").child(imagename).putBytes(data)
            uploadTask.addOnFailureListener {
                // Handle unsuccessful uploads
                Toast.makeText(this, "Snap Creation Failed, Try Again!", Toast.LENGTH_SHORT).show()

            }.addOnSuccessListener {
                // taskSnapshot.metadata contains file metadata such as size, content-type, etc.
                // ...

                val ref = FirebaseStorage.getInstance().getReference().child("image/" + imagename)
                uploadTask = ref.putBytes(data)

                val urlTask = uploadTask.continueWithTask { task ->
                    if (!task.isSuccessful) {
                        task.exception?.let {
                            throw it
                        }
                    }
                    ref.downloadUrl

                }.addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        val downloadUri = task.result
                        Log.i("SnapUpload", downloadUri.toString())

                        val intent = Intent(this, ChooseUserActivity::class.java)

                        intent.putExtra("imageURL", downloadUri.toString())
                        intent.putExtra("imageName", imagename)
                        intent.putExtra("message", messageEditText?.text.toString())
                        startActivity(intent)

                    } else {
                        // Handle failures
                        // ...
                        Toast.makeText(this, "Please Select any Image", Toast.LENGTH_SHORT).show()
                    }
                }
            }

        }catch (e : Exception){
            Toast.makeText(this, "Please Choose an Image", Toast.LENGTH_SHORT).show()
        }
    }

}

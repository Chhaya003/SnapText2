package com.khemraj.snaptext

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.AsyncTask
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage
import java.io.IOException
import java.net.HttpURLConnection
import java.net.MalformedURLException
import java.net.URL

class ViewSnapActivity : AppCompatActivity() {

    var snapImageView : ImageView? = null
    var messageTextView : TextView? = null
    val mAuth = FirebaseAuth.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_view_snap)
        setTitle("Snap and Message")

        messageTextView = findViewById(R.id.messageTextView)
        snapImageView = findViewById(R.id.snapImageView)

        messageTextView?.text = intent.getStringExtra("message")
        if(messageTextView?.text.toString().equals("")){
            messageTextView?.visibility = View.INVISIBLE
        }else{
            messageTextView?.visibility = View.VISIBLE
        }

        var task = ImageDownloader()
        var myImage: Bitmap
        try {
            myImage = task.execute(intent.getStringExtra("imageURL")).get()

            snapImageView?.setImageBitmap(myImage)

        }catch (e : Exception){
            Toast.makeText(this, "Sorry,Failed to Load the Image!!", Toast.LENGTH_SHORT).show()
        }
    }

    inner class ImageDownloader : AsyncTask<String, Void, Bitmap>() {

        override fun doInBackground(vararg urls: String): Bitmap? {
            try {
                var url = URL(urls[0])
                var httpURLConnection = url.openConnection() as HttpURLConnection
                httpURLConnection.connect()
                var input = httpURLConnection.inputStream

                return BitmapFactory.decodeStream(input)

            } catch (e: Exception) {
                e.printStackTrace()
            }


            return null
        }
    }

    override fun onBackPressed() {
        super.onBackPressed()

        FirebaseDatabase.getInstance().getReference().child("users").child(mAuth.currentUser!!.uid).child("snaps").child(intent.getStringExtra("snapKey")).removeValue()
        FirebaseStorage.getInstance().getReference().child("image").child(intent.getStringExtra("imageName")).delete()

    }
}

package com.khemraj.snaptext

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.ListView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.ChildEventListener
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase

class SnapsActivity : AppCompatActivity() {
    val mAuth = FirebaseAuth.getInstance()
    var snapsListView : ListView? = null
    var emails : ArrayList<String> = ArrayList()
    var snaps : ArrayList<DataSnapshot> = ArrayList()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_snaps)
        setTitle("Snaps You have received")
        snapsListView = findViewById(R.id.snapsListView)
        val adpater = ArrayAdapter(this, android.R.layout.simple_list_item_1, emails)
        snapsListView?.adapter = adpater

        FirebaseDatabase.getInstance().getReference().child("users").child(mAuth.currentUser!!.uid).child("snaps").addChildEventListener(object : ChildEventListener{
            override fun onChildAdded(p0: DataSnapshot, p1: String?) {
                //To change body of created functions use File | Settings | File Templates.
                emails.add(p0.child("from").value as String)
                snaps.add(p0!!)
                adpater.notifyDataSetChanged()
            }

            override fun onCancelled(p0: DatabaseError) {}
            override fun onChildMoved(p0: DataSnapshot, p1: String?) {}
            override fun onChildChanged(p0: DataSnapshot, p1: String?) {}
            override fun onChildRemoved(p0: DataSnapshot) {
                var index = 0
                for(snap : DataSnapshot in snaps){
                    if(snap.key == p0.key){
                        snaps.removeAt(index)
                        emails.removeAt(index)
                        adpater.notifyDataSetChanged()
                    }
                    index++
                }
            }
        })

        snapsListView?.onItemClickListener = AdapterView.OnItemClickListener { parent, view, position, id ->
            val snapshot = snaps.get(position)
            var intent = Intent(this, ViewSnapActivity::class.java)

            intent.putExtra("imageName", snapshot.child("imageName").value as String)
            intent.putExtra("imageURL", snapshot.child("imageUrl").value as String)
            intent.putExtra("message", snapshot.child("message").value as String)
            intent.putExtra("snapKey",snapshot.key)

            startActivity(intent)
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {

        val inflater = menuInflater
        inflater.inflate(R.menu.snaps, menu)
        return super.onCreateOptionsMenu(menu)

    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if(item.itemId == R.id.createSnaps){
            val intent = Intent(this, CreateSnapActivity::class.java)
            startActivity(intent)


        }else if (item.itemId == R.id.logout){
            mAuth.signOut()
            val intent  = Intent(this, MainActivity::class.java)
            startActivity(intent)
            finish()

        }


        return super.onOptionsItemSelected(item)
    }
}

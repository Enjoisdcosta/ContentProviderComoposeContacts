package com.example.composemaincontent

import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.os.Bundle
import android.provider.ContactsContract
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.example.composemaincontent.ui.theme.ComposeMainContentTheme
import com.google.android.material.snackbar.Snackbar
import android.Manifest
import android.content.Intent
import android.content.IntentFilter
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp


class MainActivity : ComponentActivity() {
    private val REQUEST_READ_CONTACTS: Int = 1231

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            ComposeMainContentTheme {

                ContactList(contacts = getContactList(this))

            }
        }

        loadContacts()
    }

    @Composable
    fun ContactList(contacts: List<Contact>) {
        LazyColumn {
            items(contacts) { contact ->
                ContactCard(contact)
            }
        }
    }

    data class Contact(
        val id: String,
        val name: String,
        val phoneNumber: String?,
        val email: String?
    )

    @Composable
    fun ContactCard(contact: Contact) {
        Column(modifier = Modifier
            .fillMaxSize()
            .padding(20.dp)) {
            Text(text = "ID: ${contact.id}")
            Text(text = "Name: ${contact.name}")
            Text(text = "Phone: ${contact.phoneNumber}")
            Text(text = "Email: ${contact.email ?: "None"}")
        }
    }


    private fun loadContacts() {
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.READ_CONTACTS
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            val contacts = getContactList(this)
            Log.d("Contacts", contacts.joinToString(separator = "\n"))
//            Toast.makeText(this, "Success!", Toast.LENGTH_SHORT).show()
        } else {
            requestContactsPermission();
            Toast.makeText(this, "Permission!", Toast.LENGTH_SHORT).show()
        }
    }

    private fun requestContactsPermission() {
        // Check if the permission has already been granted
        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.READ_CONTACTS
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            // Permission has already been granted, do something with the contact list
            val contacts = getContactList(this)
            Log.d("Contacts", contacts.joinToString(separator = "\n"))
            Toast.makeText(this, "Success!", Toast.LENGTH_SHORT).show()
            // Do something with the contact list
        } else {
            // Permission has not been granted, request it
            if (ActivityCompat.shouldShowRequestPermissionRationale(
                    this,
                    Manifest.permission.READ_CONTACTS
                )
            ) {
                // Explain why the app needs the permission
                // You can show a dialog or a Snackbar here
                Snackbar.make(
                    findViewById(android.R.id.content),
                    "The app needs permission to access your contacts.",
                    Snackbar.LENGTH_INDEFINITE
                ).setAction("OK") {
                    // Request the permission
                    ActivityCompat.requestPermissions(
                        this,
                        arrayOf(Manifest.permission.READ_CONTACTS),
                        REQUEST_READ_CONTACTS
                    )
                }.show()
            } else {
                // Request the permission
                ActivityCompat.requestPermissions(
                    this,
                    arrayOf(Manifest.permission.READ_CONTACTS),
                    REQUEST_READ_CONTACTS
                )
            }
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == REQUEST_READ_CONTACTS && grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            // Permission has been granted, do something with the contact list
            val contacts = getContactList(this)
            Log.d("Contacts", contacts.joinToString(separator = "\n"))
            Toast.makeText(this, "Success!", Toast.LENGTH_SHORT).show()
            // Do something with the contact list
        } else {
            // Permission has been denied
            // You can show a dialog or a Snackbar here to explain why the app needs the permission
        }
    }

    @SuppressLint("Range")
    fun getContactList(context: Context): List<Contact> {
        val contacts = mutableListOf<Contact>()
        val contentResolver = context.contentResolver
        val cursor = contentResolver.query(
            ContactsContract.Contacts.CONTENT_URI,
            null, null, null, null
        )
        cursor?.use {
            while (it.moveToNext()) {
                val id = it.getString(it.getColumnIndex(ContactsContract.Contacts._ID))
                val name = it.getString(it.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME))
                val email: String? = null
                val hasPhoneNumber =
                    it.getInt(it.getColumnIndex(ContactsContract.Contacts.HAS_PHONE_NUMBER)) > 0
                if (hasPhoneNumber) {
                    val phoneCursor = contentResolver.query(
                        ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                        null,
                        ContactsContract.CommonDataKinds.Phone.CONTACT_ID + " = ?",
                        arrayOf(id),
                        null
                    )
                    phoneCursor?.use { pc ->
                        while (pc.moveToNext()) {
                            val phoneNumber =
                                pc.getString(pc.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER))
                            val id =
                                pc.getString(pc.getColumnIndex(ContactsContract.CommonDataKinds.Phone.CONTACT_ID))
                            val contact = Contact(id, name, phoneNumber, email)
                            contacts.add(contact)
                        }
                    }
                    phoneCursor?.close()
                } else {
                    val contact = Contact(id, name, null, email)
                    contacts.add(contact)
                }
            }
        }
        cursor?.close()
        return contacts
    }


}


package com.example.appatemporal.domain

import android.util.Log
import com.example.appatemporal.domain.models.UserModel
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.tasks.await

class FirestoreService {
    private val db = Firebase.firestore

    fun addUser(uid: String, user: UserModel) {
        db.collection("Usuario")
            .document(uid)
            .set(user)
            .addOnSuccessListener {
                Log.d("FirestoreLogs","Added User Correctly")
            }
            .addOnFailureListener {
                Log.d("FirestoreLogs","Added user failed, exception: $it")
            }
    }

    fun addRole(role: String) {
        val dbRole = hashMapOf<String, String>(
            "nombre_Rol" to role
        )

        db.collection("Rol")
            .document(role)
            .set(dbRole)
            .addOnSuccessListener {
                Log.d("FirestoreLogs","Added Role Correctly")
            }
            .addOnFailureListener {
                Log.d("FirestoreLogs","Added user failed, exception: $it")
            }
    }

    fun addUserRole(uid: String, role: String) {
        val userRole = hashMapOf(
            "id_Usuario" to uid,
            "id_Rol" to role
        )
        db.collection("Usuario_Rol")
            .add(userRole)
            .addOnSuccessListener {
                Log.d("FirestoreLogs","Added User wih Role Correctly")
            }
            .addOnFailureListener {
                Log.d("FirestoreLogs","Added user failed, exception: $it")
            }
    }

    suspend fun verifyUser(uid: String) : Boolean {
        var userExists = false
        db.collection("Usuario")
            .document(uid)
            .get()
            .addOnSuccessListener { documentRef ->
                if (documentRef.exists()) {
                    userExists = true
                }
            }
            .await()
        return userExists
    }

    suspend fun getUser(uid: String) : DocumentSnapshot{
        var userData: DocumentSnapshot =
            db.collection("Usuario")
            .document(uid)
            .get()
            .await()
        return userData
    }
}
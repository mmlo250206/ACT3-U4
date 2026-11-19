package com.example.music_app4e

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.credentials.Credential
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import androidx.credentials.exceptions.GetCredentialException
import androidx.lifecycle.lifecycleScope
import com.google.android.libraries.identity.googleid.GetSignInWithGoogleOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.android.libraries.identity.googleid.GoogleIdTokenParsingException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {

    // ATRIBUTOS PARA LAS CREDENCIALES DE LOS PROVEEDORES (FIREBASE + GOOGLE)
    private lateinit var auth: FirebaseAuth
    private lateinit var credentialManager: CredentialManager
    private lateinit var btnGoogle: ImageButton

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)

        val dbHelper = DatabaseHelper(this)
        val etCorreo = findViewById<EditText>(R.id.etCorreoLogin)
        val etPassword = findViewById<EditText>(R.id.etPasswordLogin)
        val btnIngresar = findViewById<Button>(R.id.btnIngresar)

        btnGoogle = findViewById(R.id.btnGoogle)
        auth = FirebaseAuth.getInstance()
        credentialManager = CredentialManager.create(this)

        //INICIAR SESION CON LOS USUARIOS GUARDADOS EN LA BD
        btnIngresar.setOnClickListener {
            val usuario = etCorreo.text.toString().trim()
            val pass = etPassword.text.toString().trim()

            if (usuario.isEmpty() || pass.isEmpty()) {
                Toast.makeText(this, "Completa todos los campos", Toast.LENGTH_SHORT).show()
            } else if (dbHelper.validarUsuario(usuario, pass)) {
                Toast.makeText(this, "Bienvenido $usuario", Toast.LENGTH_SHORT).show()
                //IR A LA PANTALLA PRINCIPAL
                val intent = Intent(this, HomeActivity::class.java)
                startActivity(intent)
                finish()
            } else {
                Toast.makeText(this, "Usuario o contraseña incorrectos", Toast.LENGTH_SHORT).show()
            }
        }

        //INICIAR SESION CON GOOGLE
        btnGoogle.setOnClickListener {
            iniciarSesionGoogle()
        }

        val tvCrearCuenta = findViewById<TextView>(R.id.tvCrearCuenta)
        //crear evento para ir a otra pantalla
        tvCrearCuenta.setOnClickListener {
            val intent = Intent(this, RegisterActivity::class.java)
            startActivity(intent)
        }

    }

    // 3. MÉTODO PARA AUTENTICAR EN FIREBASE
    private fun autenticarFirebase(idToken: String) {

        val credencialFirebase = GoogleAuthProvider.getCredential(idToken, null)

        auth.signInWithCredential(credencialFirebase).addOnCompleteListener(this) { inicio ->

            // Validar que el inicio de sesión del usuario haya sido correcto
            if (inicio.isSuccessful) {

                // Variable que guarda la info del usuario
                val usuario = auth.currentUser

                Toast.makeText(this, "Bienvenido ${usuario?.displayName}", Toast.LENGTH_SHORT).show()

                val intent = Intent(this, HomeActivity::class.java)
                startActivity(intent)
                finish()

            } else {
                Toast.makeText(this, "Error al iniciar con Firebase", Toast.LENGTH_SHORT).show()
            }
        }
    }

    // 4. MÉTODO PARA PROCESAR LA CREDENCIAL
    private fun procesarCredencial(credential: Credential) {

        if (credential is CustomCredential && credential.type ==
            GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL) {

            try {
                // Variable que permite identificar la credencial del usuario
                val credencialGoogle = GoogleIdTokenCredential.createFrom(credential.data)

                autenticarFirebase(credencialGoogle.idToken)

            } catch (e: GoogleIdTokenParsingException) {
                Toast.makeText(this, "Credenciales incorrectas", Toast.LENGTH_SHORT).show()
            }
        }
    }

    // 5. MÉTODO PARA INICIAR SESIÓN CON GOOGLE
    private fun iniciarSesionGoogle() {

        val inicioGoogle = GetSignInWithGoogleOption.Builder(
            getString(R.string.default_web_client_id)).build()

        val solicitud = GetCredentialRequest.Builder()
            .addCredentialOption(inicioGoogle)
            .build()

        lifecycleScope.launch {
            try {
                // Verificar si las credenciales del usuario pueden ser utilizadas
                val resultado = credentialManager.getCredential(
                    context = this@MainActivity,
                    request = solicitud
                )

                // Procesar credencial para ver si el usuario entra o no
                procesarCredencial(resultado.credential)

            } catch (e: GetCredentialException) {
                Toast.makeText(this@MainActivity, "Error en las credenciales del usuario", Toast.LENGTH_SHORT).show()
            }
        }
    }

    // MÉTODO PARA MANTENER LAS SESIONES ACTIVAS
    override fun onStart() {
        super.onStart()

        // Info del usuario actual
        val usuarioActual = auth.currentUser

        if(usuarioActual != null){
            val intent = Intent(this, HomeActivity::class.java)
            startActivity(intent)
            finish()
        }

    }
}

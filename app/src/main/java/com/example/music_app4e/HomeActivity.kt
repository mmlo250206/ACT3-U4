package com.example.music_app4e

import android.content.Intent
import android.os.Bundle
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.credentials.CredentialManager
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch
import androidx.core.view.WindowInsetsCompat
import com.bumptech.glide.Glide
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.auth.FirebaseAuth
import com.example.music_app4e.model.Pelicula
import com.example.music_app4e.model.PeliculasResponse
import com.example.music_app4e.network.RetrofitClient
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class HomeActivity : AppCompatActivity() {

    private lateinit var tvEstado : TextView
    private lateinit var contenedorAccion : LinearLayout
    private lateinit var contenedorComedia : LinearLayout
    private lateinit var contenedorTerror : LinearLayout
    private lateinit var bottomNavigation : BottomNavigationView

    private lateinit var auth : FirebaseAuth
    private lateinit var credentialManager : CredentialManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_home)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        tvEstado = findViewById(R.id.tvEstado)
        contenedorAccion = findViewById(R.id.contenedorAccion)
        contenedorComedia = findViewById(R.id.contenedorComedia)
        contenedorTerror = findViewById(R.id.contenedorTerror)
        bottomNavigation = findViewById(R.id.bottomNavigation)

        auth = FirebaseAuth.getInstance()
        credentialManager = CredentialManager.create(this)

        // 3 CONSULTAS AL API (UNA POR GÉNERO)
        // 4401 = Acción y Aventura, 4404 = Comedia, 4408 = Terror
        obtenerPeliculas(4401, contenedorAccion)
        obtenerPeliculas(4404, contenedorComedia)
        obtenerPeliculas(4408, contenedorTerror)

        menuNavegacion()

    }

    //METODO PARA OBTENER LA RESPUESTA DEL API
    private fun obtenerPeliculas(genero : Int, contenedor : LinearLayout){

        RetrofitClient.api.obtenerPeliculasGenero(

            pais = "mx",
            limite = 15,
            genero = genero

        ).enqueue(object : Callback<PeliculasResponse>{

            //METODO EN CASO DE OBTENER UNA RESPUESTA EXITOSA
            override fun onResponse(call: Call<PeliculasResponse>, response: Response<PeliculasResponse>){

                //VARIABLE QUE OBTIENE LA LISTA DE PELÍCULAS DEL API
                val peliculas = response.body()?.feed?.entry ?: emptyList()

                if(peliculas.isNotEmpty()){

                    tvEstado.text = "Películas cargadas"
                    mostrarPeliculas(peliculas, contenedor)

                }else{
                    Toast.makeText(this@HomeActivity, "No Hay Resultados", Toast.LENGTH_SHORT).show()

                }

            }

            //METODO EN CASO DE UNA RESPUESTA FALLIDA
            override fun onFailure(call: Call<PeliculasResponse>, t: Throwable) {
                Toast.makeText(this@HomeActivity, "Error en el API", Toast.LENGTH_SHORT).show()
            }

        })

    }

    // MÉTODO PARA MOSTRAR UNA LISTA DE PELÍCULAS
    private fun mostrarPeliculas(peliculas : List<Pelicula>, contenedor : LinearLayout){

        // Eliminar los elementos duplicados
        contenedor.removeAllViews()

        // Ciclo que recorre las películas
        for(pelicula in peliculas){

            // Crear la tarjeta (item_pelicula)
            val vistaPelicula = layoutInflater.inflate(
                R.layout.item_pelicula,
                contenedor,
                false
            )

            // Obtener los elementos de la tarjeta
            val imgPoster = vistaPelicula.findViewById<ImageView>(R.id.imgPoster)
            val tvTitulo = vistaPelicula.findViewById<TextView>(R.id.tvTitulo)
            val tvDirector = vistaPelicula.findViewById<TextView>(R.id.tvDirector)

            tvTitulo.text = pelicula.nombre?.label ?: "Película sin nombre"
            tvDirector.text = pelicula.director?.label ?: "Director desconocido"

            Glide.with(this)
                .load(pelicula.obtenerPosterGrande())
                .into(imgPoster)

            // PASAR INFORMACIÓN A OTRA PANTALLA
            vistaPelicula.setOnClickListener {

                val idPelicula = pelicula.id?.attributes?.idPelicula?.toLongOrNull()

                if(idPelicula != null){

                    val intent = Intent(this@HomeActivity, DetailsActivity::class.java)

                    intent.putExtra("ID_PELICULA", idPelicula)
                    intent.putExtra("NOMBRE_PELICULA", pelicula.nombre?.label)
                    intent.putExtra("DIRECTOR_PELICULA", pelicula.director?.label)
                    intent.putExtra("GENERO_PELICULA", pelicula.categoria?.attributes?.label)
                    intent.putExtra("FECHA_PELICULA", pelicula.fechaEstreno?.label)
                    intent.putExtra("SINOPSIS_PELICULA", pelicula.sinopsis?.label)
                    intent.putExtra("IMAGEN_PELICULA", pelicula.obtenerPosterGrande())

                    startActivity(intent)

                }

            }

            contenedor.addView(vistaPelicula)

        }

    }

    // MÉTODO PARA USAR EL MENÚ DE NAVEGACIÓN
    private fun menuNavegacion(){

        // Indicar cual es el item seleccionado
        bottomNavigation.selectedItemId = R.id.nav_inicio

        // Indicar cual es el item al que se va a ir el usuario
        bottomNavigation.setOnItemSelectedListener { item ->

            when(item.itemId){

                R.id.nav_inicio -> {
                    true
                }

                R.id.nav_favoritos -> {
                    val intent = Intent(this, FavoritesActivity::class.java)
                    startActivity(intent)
                    true
                }

                R.id.nav_salir -> {
                    cerrarSesionGoogle()
                    true
                }

                else -> false

            }

        }

    }

    // MÉTODO PARA CERRAR SESIÓN
    private fun cerrarSesionGoogle(){

        auth.signOut()

        lifecycleScope.launch {

            try {

                credentialManager.clearCredentialState(
                    androidx.credentials.ClearCredentialStateRequest()
                )

            }catch (e: Exception){
                Toast.makeText(this@HomeActivity, "Error al cerrar sesión", Toast.LENGTH_SHORT).show()
            }

            val intent = Intent(this@HomeActivity, MainActivity::class.java)

            // Indicar que se borre toda la info y la aplicación comience desde cero
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK

            startActivity(intent)

        }

    }

}

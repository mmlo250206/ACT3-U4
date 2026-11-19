package com.example.music_app4e

import android.os.Bundle
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.bumptech.glide.Glide

class DetailsActivity : AppCompatActivity() {

    private lateinit var imgPosterDetalle : ImageView
    private lateinit var tvNombrePeliculaDetalle : TextView
    private lateinit var tvDirectorDetalle : TextView
    private lateinit var tvPeliculaDetalle : TextView
    private lateinit var tvSinopsisDetalle : TextView
    private lateinit var btnFavorito : ImageButton

    private var peliculaFavorita = false

    private lateinit var dbHelper : DatabaseHelper

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_details)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        imgPosterDetalle = findViewById(R.id.imgPosterDetalle)
        tvNombrePeliculaDetalle = findViewById(R.id.tvNombrePeliculaDetalle)
        tvDirectorDetalle = findViewById(R.id.tvDirectorDetalle)
        tvPeliculaDetalle = findViewById(R.id.tvPeliculaDetalle)
        tvSinopsisDetalle = findViewById(R.id.tvSinopsisDetalle)
        btnFavorito = findViewById(R.id.btnFavorito)

        dbHelper = DatabaseHelper(this)

        mostrarInformacionPelicula()

    }

    // MÉTODO PARA OBTENER LA INFORMACIÓN QUE VIENE DEL API
    private fun mostrarInformacionPelicula(){

        val idPelicula = intent.getLongExtra("ID_PELICULA", 1)
        val nombrePelicula = intent.getStringExtra("NOMBRE_PELICULA") ?: "Película desconocida"
        val director = intent.getStringExtra("DIRECTOR_PELICULA") ?: "Director desconocido"
        val genero = intent.getStringExtra("GENERO_PELICULA") ?: "Sin género"
        val fecha = intent.getStringExtra("FECHA_PELICULA") ?: "Sin fecha"
        val sinopsis = intent.getStringExtra("SINOPSIS_PELICULA") ?: "Sin sinopsis"
        val imagen = intent.getStringExtra("IMAGEN_PELICULA") ?: ""

        tvNombrePeliculaDetalle.text = nombrePelicula
        tvDirectorDetalle.text = director
        tvPeliculaDetalle.text = genero + " - " + fecha.take(10)
        tvSinopsisDetalle.text = sinopsis

        Glide.with(this)
            .load(imagen)
            .centerCrop()
            .into(imgPosterDetalle)

        // Se verifica desde la base de datos si la película es favorita
        if (idPelicula != -1L) {
            peliculaFavorita = dbHelper.existePeliculaFavorita(idPelicula)
        }
        actualizarIcono()

        // BOTÓN DE FAVORITOS: GUARDA O ELIMINA LA PELÍCULA EN LA BD
        btnFavorito.setOnClickListener {

            if (peliculaFavorita) {

                val eliminado = dbHelper.eliminarPeliculaFavorita(idPelicula)

                if (eliminado) {
                    peliculaFavorita = false
                    Toast.makeText(this, "Película $nombrePelicula eliminada de favoritos", Toast.LENGTH_SHORT).show()
                }

            } else {

                val guardado = dbHelper.guardarPeliculaFavorita(
                    idPelicula = idPelicula,
                    nombrePelicula = nombrePelicula,
                    director = director,
                    genero = genero,
                    fecha = fecha,
                    sinopsis = sinopsis,
                    imagen = imagen
                )

                if (guardado) {
                    peliculaFavorita = true
                    Toast.makeText(this, "Película $nombrePelicula agregada a favoritos", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(this, "Película $nombrePelicula ya está en favoritos", Toast.LENGTH_SHORT).show()
                }
            }

            // Actualizar el ícono después de guardar o eliminar
            actualizarIcono()
        }

    }

    // MÉTODO PARA ACTUALIZAR EL ÍCONO DEL BOTÓN SEGÚN SI LA PELÍCULA ES FAVORITA
    private fun actualizarIcono(){
        if(peliculaFavorita){
            btnFavorito.setImageResource(R.drawable.favorito)
        }else{
            btnFavorito.setImageResource(R.drawable.no_favorito)
        }
    }

}

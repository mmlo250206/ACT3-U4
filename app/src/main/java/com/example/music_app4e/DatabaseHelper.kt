package com.example.music_app4e

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

class DatabaseHelper(context: Context) : SQLiteOpenHelper(

    context,
    DATABASE_NAME,
    null,
    DATABASE_VERSION

){

    companion object{
        private const val DATABASE_NAME = "musica.db"
        private const val DATABASE_VERSION = 2
        private const val TABLE_USUARIOS = "usuarios"
        private const val COL_ID= "id"
        private const val COL_NOMBRE= "nombre"
        private const val COL_CORREO= "correo"
        private const val COL_PASSWORD= "password"

        //TABLA DE PELICULAS FAVORITAS
        private const val TABLE_FAVORITOS = "favoritos"
        private const val COL_ID_FAVORITO = "id"
        private const val COL_ID_PELICULA = "pelicula_id"
        private const val COL_NOMBRE_PELICULA = "nombre_pelicula"
        private const val COL_DIRECTOR = "director"
        private const val COL_GENERO = "genero"
        private const val COL_FECHA = "fecha"
        private const val COL_SINOPSIS = "sinopsis"
        private const val COL_IMAGEN = "imagen"

        private const val CREATE_TABLE_USUARIOS = """
            CREATE TABLE $TABLE_USUARIOS(
            $COL_ID INTEGER PRIMARY KEY AUTOINCREMENT,
            $COL_NOMBRE TEXT NOT NULL,
            $COL_CORREO TEXT NOT NULL UNIQUE,
            $COL_PASSWORD TEXT NOT NULL
            )
        """

        private const val CREATE_TABLE_FAVORITOS = """
            CREATE TABLE $TABLE_FAVORITOS(
            $COL_ID_FAVORITO INTEGER PRIMARY KEY AUTOINCREMENT,
            $COL_ID_PELICULA INTEGER UNIQUE NOT NULL,
            $COL_NOMBRE_PELICULA TEXT NOT NULL,
            $COL_DIRECTOR TEXT,
            $COL_GENERO TEXT,
            $COL_FECHA TEXT,
            $COL_SINOPSIS TEXT,
            $COL_IMAGEN TEXT
            )
        """
    }

    //METODO PARA CREAR LA BASE DE DATOS Y SUS TABLAS
    override fun onCreate(db: SQLiteDatabase?) {
        db?.execSQL(CREATE_TABLE_USUARIOS)
        db?.execSQL(CREATE_TABLE_FAVORITOS)
    }
    //METODO PARA ACTUALIZAR LA BASE DE DATOS
    override fun onUpgrade(db: SQLiteDatabase?, oldVersion: Int, newVersion: Int) {
        //Validar la version actual de la BD (si venia de la 1, solo se agrega la tabla de favoritos)
        if (oldVersion < 2) {
            db?.execSQL(CREATE_TABLE_FAVORITOS)
        }
    }

    //METODO PARA REGISTRAR UN USUARIO(INSERTAR)
    fun registrarUsuario(nombre: String, correo: String, password: String): Boolean{

        //crear vatiable con permisos para escribir en la bd
        val db = writableDatabase
        //crear variables para guardar la informacion
        val datos = ContentValues().apply {
            put(COL_NOMBRE, nombre)
            put(COL_CORREO, correo)
            put(COL_PASSWORD, password)
        }

        val resultado = db.insert(TABLE_USUARIOS, null, datos)

        return resultado != -1L
    }

    //METODO PARA SABER SI UN CORREO YA ESTA REGISTRADO
    fun existeCorreo(correo: String): Boolean {
        val db = readableDatabase
        val cursor = db.rawQuery(
            "SELECT $COL_ID FROM $TABLE_USUARIOS WHERE $COL_CORREO = ?",
            arrayOf(correo)
        )
        val existe = cursor.count > 0
        cursor.close()
        return existe
    }

    //METODO PARA INICIAR SESION (BUSCA POR CORREO O NOMBRE DE USUARIO)
    fun validarUsuario(usuario: String, password: String): Boolean {
        val db = readableDatabase
        val cursor = db.rawQuery(
            "SELECT $COL_ID FROM $TABLE_USUARIOS WHERE ($COL_CORREO = ? OR $COL_NOMBRE = ?) AND $COL_PASSWORD = ?",
            arrayOf(usuario, usuario, password)
        )
        val valido = cursor.count > 0
        cursor.close()
        return valido
    }

    //METODO PARA GUARDAR UNA PELICULA FAVORITA (INSERTAR UN REGISTRO QUE VIENE DEL API)
    fun guardarPeliculaFavorita(idPelicula: Long, nombrePelicula: String, director: String, genero: String,
                                fecha: String, sinopsis: String, imagen: String): Boolean {

        val db = writableDatabase

        val datos = ContentValues().apply {
            put(COL_ID_PELICULA, idPelicula)
            put(COL_NOMBRE_PELICULA, nombrePelicula)
            put(COL_DIRECTOR, director)
            put(COL_GENERO, genero)
            put(COL_FECHA, fecha)
            put(COL_SINOPSIS, sinopsis)
            put(COL_IMAGEN, imagen)
        }

        //CONFLICT_IGNORE: si la pelicula ya esta en favoritos (pelicula_id es UNIQUE) no la repite
        val resultado = db.insertWithOnConflict(
            TABLE_FAVORITOS,
            null,
            datos,
            SQLiteDatabase.CONFLICT_IGNORE
        )

        return resultado != -1L
    }

    //METODO PARA ELIMINAR UNA PELICULA DE FAVORITOS
    fun eliminarPeliculaFavorita(idPelicula: Long): Boolean {
        val db = writableDatabase

        val resultado = db.delete(
            TABLE_FAVORITOS,
            "$COL_ID_PELICULA = ?",
            arrayOf(idPelicula.toString())
        )

        //delete regresa cuantas filas borro
        return resultado > 0
    }

    //METODO PARA SABER SI UNA PELICULA YA ESTA EN FAVORITOS
    fun existePeliculaFavorita(idPelicula: Long): Boolean {

        val db = readableDatabase

        val cursor = db.rawQuery(
            "SELECT $COL_ID_FAVORITO FROM $TABLE_FAVORITOS WHERE $COL_ID_PELICULA = ?",
            arrayOf(idPelicula.toString())
        )

        //moveToFirst() es true si encontro al menos un registro
        val existe = cursor.moveToFirst()

        cursor.close()

        return existe
    }
}
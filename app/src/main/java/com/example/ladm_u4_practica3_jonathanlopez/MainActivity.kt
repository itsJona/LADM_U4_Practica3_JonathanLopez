package com.example.ladm_u4_practica3_jonathanlopez

import android.app.Dialog
import android.content.pm.PackageManager
import android.database.sqlite.SQLiteException
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.core.app.ActivityCompat
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.activity_main.txtSaldo
import java.util.ArrayList

class MainActivity : AppCompatActivity() {

    var listaID = ArrayList<String>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        listarClientes()


        btnRegistrar.setOnClickListener {

            if (ActivityCompat.checkSelfPermission(
                    this,
                    android.Manifest.permission.SEND_SMS
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                ActivityCompat.requestPermissions(
                    this,
                    arrayOf(
                        android.Manifest.permission.SEND_SMS,
                        android.Manifest.permission.RECEIVE_SMS,
                        android.Manifest.permission.READ_SMS
                    ),
                    1
                )
            } else {

                if (txtNoTarjeta.text.isEmpty() || txtNombre.text.isEmpty() || txtSaldo.text.isEmpty()) {
                    Toast.makeText(
                        this,
                        "Todos los campos deben tener datos antes de registrar.",
                        Toast.LENGTH_LONG
                    ).show()
                }
                try {
                    var baseDatos = BaseDeDatos(this, "cuenta", null, 1)
                    var insertar = baseDatos.writableDatabase
                    var sql =
                        "INSERT INTO CUENTA VALUES ('${txtNoTarjeta.text.toString()}','${txtNombre.text.toString()}',${txtSaldo.text.toString()
                            .toDouble()})"
                    insertar.execSQL(sql)
                    baseDatos.close()
                    Toast.makeText(this, "Se registró con éxito", Toast.LENGTH_LONG).show()
                } catch (err: SQLiteException) {
                    Toast.makeText(
                        this,
                        "Error: Numero de tarjeta no válido o ya exíste un cliente con este número de tarjeta ",
                        Toast.LENGTH_LONG
                    ).show()
                }
                listarClientes()
            }
        }
    }


    private fun listarClientes(){

        try{
            var cursor = BaseDeDatos(this,"cuenta",null,1)
                .readableDatabase
                .rawQuery("SELECT * FROM CUENTA", null)
            var registro=""
            var arreglo = ArrayList<String>()

            arreglo.clear()
            listaID.clear()
            if(cursor.count>0){
                cursor.moveToFirst()
                var cantidad = cursor.count-1
                (0..cantidad).forEach{
                    registro ="\nNo.Tarjeta: "+cursor.getString(0)+
                              "\nNombre: "+cursor.getString(1)+
                              "\nSaldo: $"+cursor.getDouble(2) +"\n"

                    arreglo.add(registro)
                    listaID.add(cursor.getString(0))
                    cursor.moveToNext()
                }
                lista.adapter = ArrayAdapter<String> (this,android.R.layout.simple_list_item_1,arreglo)
                lista.setOnItemClickListener { parent, view, position, id ->
                    AlertDialog.Builder(this)
                        .setTitle("Atención")
                        .setMessage("¿Qué desea hacer con el cliente: "+listaID[position])
                        .setPositiveButton("Eliminar"){d,i->
                            eliminarPorID(listaID[position])
                        }
                        .setNegativeButton("Cancelar") {d,i->
                            d.dismiss()
                        }
                        .show()

                }
            }else{
                registro="Sin clientes registrados."
                arreglo.add(registro)
                lista.adapter = ArrayAdapter<String> (this,android.R.layout.simple_list_item_1,arreglo)
            }
        }catch (err:SQLiteException){
            Toast.makeText(this,"Error:"+err.message,Toast.LENGTH_LONG).show()
        }
    }
    fun eliminarPorID(numTarjeta: String) {
        try{
            var baseDatos=BaseDeDatos(this,"cuenta",null,1)
            var eliminar = baseDatos.writableDatabase
            var query = "DELETE FROM CUENTA WHERE NUMERO_TARJETA = '${numTarjeta}' "
            eliminar.execSQL(query)
            Toast.makeText(this,"Se eliminó el cliente con éxito",Toast.LENGTH_LONG).show()
            eliminar.close()
            baseDatos.close()
            listarClientes()
        }catch (error:SQLiteException){
            Toast.makeText(this,"Error: "+ error,Toast.LENGTH_LONG).show()
        }

    }

}

package com.example.ladm_u4_practica3_jonathanlopez

import android.app.AlertDialog
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.database.sqlite.SQLiteException
import android.os.Build
import android.telephony.SmsManager
import android.telephony.SmsMessage
import android.widget.Toast
import com.google.android.material.internal.DescendantOffsetUtils
import kotlinx.android.synthetic.main.activity_main.*

class SmsService : BroadcastReceiver(){
    override fun onReceive(context: Context, intent: Intent) {
        //LEER MENSAJES
        val extras = intent.extras
        if(extras != null){
            var sms = extras.get("pdus") as Array<Any>
            for(indice in sms.indices) {
                var formato = extras.getString("format")
                var smsMensaje = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) { SmsMessage.createFromPdu(sms[indice] as ByteArray, formato)
                } else {
                    SmsMessage.createFromPdu(sms[indice] as ByteArray)
                }
                var celularOrigen = smsMensaje.originatingAddress
                var contenidoSMS = smsMensaje.messageBody.toString()


                var saldo:Double =0.0
                var nombre = ""

                if(validarFormato(contenidoSMS)==true) {

                    var numeroTarjeta=""
                    var nombreTitular=""

                    if (contenidoSMS.length > 11) { numeroTarjeta =contenidoSMS.substring(0, 12)}
                    if (contenidoSMS.length > 13) { nombreTitular =contenidoSMS.substring(13, contenidoSMS.length)}

                    try {
                        var cursor = BaseDeDatos(context, "cuenta", null, 1)
                            .readableDatabase.rawQuery(
                                "SELECT * FROM CUENTA WHERE NUMERO_TARJETA = '${numeroTarjeta}'",
                                null)
                        if (cursor.count > 0) {
                            cursor.moveToFirst()
                            if(cursor.getString(1).equals(nombreTitular)){
                            SmsManager.getDefault().sendTextMessage(
                                celularOrigen, null,
                                                               "Su saldo actual en MyBank es de: $${cursor.getDouble(2)}. ¡Gracias por utilizar nuestro servicio movil de consulta de saldo!",
                                null, null
                            )}else {//mensaje no encontrado
                                SmsManager.getDefault().sendTextMessage(
                                    celularOrigen, null, "Lo sentimos, no fue posible consultar su saldo en MyBank. Verifique sus datos o si se encuentra afiliado a MyBank.",
                                    null, null)
                            }
                        } else {//mensaje no encontrado
                            SmsManager.getDefault().sendTextMessage(
                                celularOrigen, null, "Lo sentimos, no fue posible consultar su saldo en MyBank. Verifique sus datos o si se encuentra afiliado a MyBank.",
                                null, null)
                        }
                    } catch (err: SQLiteException) {
                        Toast.makeText(context, "Error:" + err.message, Toast.LENGTH_LONG).show()
                    }
                }else {
                    SmsManager.getDefault().sendTextMessage(
                        celularOrigen, null,
                        "Formato incorrecto. El formato debe ser: [No. Tarjeta]-[NOMBRECOMPLETO (Por apellidos)]. Ejemplo: 123456789012-LOPEZSANCHEZJONATHANISRAEL",
                        null, null)
                }
            }
        }
    }


    fun validarFormato(sms:String):Boolean {

        var flag = true
        if(sms.length<=13){return false }
        //Verificar formato de numero (12 digitos)
        if (sms.length > 12) {
            (0..11).forEach {
                if (sms.substring(it, it + 1).equals("0") ||
                    sms.substring(it, it + 1).equals("1") ||
                    sms.substring(it, it + 1).equals("2") ||
                    sms.substring(it, it + 1).equals("3") ||
                    sms.substring(it, it + 1).equals("4") ||
                    sms.substring(it, it + 1).equals("5") ||
                    sms.substring(it, it + 1).equals("6") ||
                    sms.substring(it, it + 1).equals("7") ||
                    sms.substring(it, it + 1).equals("8") ||
                    sms.substring(it, it + 1).equals("9")
                ) { } else { return false }
            }
        }
        //Verificar guion (-)
        if (sms.length > 12) {if(!sms.substring(12,13).equals("-")){return false}}
        //Verificar que nombre no tenga numeros
        if (sms.length > 13) {
            (13..sms.length-1).forEach {
                if (sms.substring(it, it + 1).equals("0") ||
                    sms.substring(it, it + 1).equals("1") ||
                    sms.substring(it, it + 1).equals("2") ||
                    sms.substring(it, it + 1).equals("3") ||
                    sms.substring(it, it + 1).equals("4") ||
                    sms.substring(it, it + 1).equals("5") ||
                    sms.substring(it, it + 1).equals("6") ||
                    sms.substring(it, it + 1).equals("7") ||
                    sms.substring(it, it + 1).equals("8") ||
                    sms.substring(it, it + 1).equals("9")
                ) {return false}
            }
        }

        return flag
    }
}
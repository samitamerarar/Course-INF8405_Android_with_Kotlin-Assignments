package com.example.myapplication.RecyclerViews

import android.app.AlertDialog
import android.content.Intent
import android.graphics.Color
import android.net.Uri
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import androidx.recyclerview.widget.RecyclerView
import com.example.myapplication.MainActivity
import com.example.myapplication.R
import de.cketti.mailto.EmailIntentBuilder
import kotlinx.android.synthetic.main.adapter_details.view.*
import org.w3c.dom.Text
import java.util.*


class AdapterScanResults(private val bluetoothList: ArrayList<ModelDetails>) : RecyclerView.Adapter<AdapterScanResults.ViewHolder>() {

    // Retourne la vue pour chaque element de la liste
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view =
            LayoutInflater.from(parent.context).inflate(R.layout.adapter_details, parent, false)
        return ViewHolder(
            view
        )
    }

    // Rattache les elements a la liste
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bindItems(bluetoothList[position])
    }

    override fun getItemCount(): Int {
        return bluetoothList.size
    }

    // Classe qui contient la vue de la liste
    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        fun bindItems(bluetoothItem: ModelDetails) {
            toggleDarkMode(itemView)
            itemView.bluetooth_distance.text = "approx. ${bluetoothItem.distance} meters away"
            itemView.bluetooth_name.text = bluetoothItem.name
            itemView.bluetooth_description.text = bluetoothItem.macaddress

            itemView.setOnClickListener {
                val dialogBuilder = AlertDialog.Builder(itemView.context)
                dialogBuilder.setMessage(
                        bluetoothItem.description)
                    .setCancelable(true)
                    .setPositiveButton("Comment y arriver") { _, _ ->
                        val uri = java.lang.String.format(
                            Locale.ENGLISH,
                            "http://maps.google.com/maps?daddr=%f,%f (%s)",
                            bluetoothItem.position!!.latitude,
                            bluetoothItem.position.longitude,
                            bluetoothItem.name
                        )
                        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(uri))
                        intent.setPackage("com.google.android.apps.maps")
                        itemView.context.startActivity(intent)
                    }
                    .setNegativeButton("Partager") { _, _ ->
                        EmailIntentBuilder.from(itemView.context)
                            .subject("Bluetooth Localizer - details de l'appareil")
                            .body("Voici les details demandés pour <${bluetoothItem.name}> :\n" +
                                    "${bluetoothItem.description}")
                            .start();
                    }
                    .setNeutralButton("Ajouter aux favoris") { dialog, _ ->
                        val intent = Intent("put-favorite")
                        intent.putExtra("name", bluetoothItem.name)
                        intent.putExtra("macaddress", bluetoothItem.macaddress)
                        intent.putExtra("description", bluetoothItem.description)
                        LocalBroadcastManager.getInstance(itemView.context).sendBroadcast(intent)
                        dialog.cancel()
                    }

                // create dialog box
                val alert = dialogBuilder.create()
                alert.setCustomTitle(customTitleForAlertDialog(MainActivity.darkMode, bluetoothItem.name))
                alert.show()
                alert.findViewById<TextView>(android.R.id.message).gravity = Gravity.CENTER

                if (MainActivity.darkMode) {
                    alert.window!!.setBackgroundDrawableResource(android.R.color.background_dark);
                    alert.findViewById<TextView>(android.R.id.message).setTextColor(Color.parseColor("#ffffff"))
                }
                else {
                    alert.window!!.setBackgroundDrawableResource(android.R.color.background_light);
                    alert.findViewById<TextView>(android.R.id.message).setTextColor(Color.parseColor("#000000"))
                }
            }
        }

        private fun customTitleForAlertDialog(darkMode: Boolean, bluetoothItemName: String): TextView {
            val textView = TextView(itemView.context)
            textView.setPadding(20, 30, 20, 30)
            textView.textSize = 20f
            textView.text = bluetoothItemName
            if (darkMode) textView.setTextColor(Color.WHITE) else textView.setTextColor(Color.BLACK)
            return textView
        }

        private fun toggleDarkMode(itemView: View) {
            with(itemView) {
                if (MainActivity.darkMode) {
                    bluetooth_item.setBackgroundColor(Color.parseColor("#121212"))
                    bluetooth_item_body.setBackgroundColor(Color.parseColor("#121212"))
                    bluetooth_name.setTextColor(Color.parseColor("#ffffff"))
                    bluetooth_description.setTextColor(Color.parseColor("#ffffff"))
                    bluetooth_distance.setTextColor(Color.parseColor("#ffffff"))
                }
                else {
                    bluetooth_item.setBackgroundColor(Color.parseColor("#FFFFFF"))
                    bluetooth_item_body.setBackgroundColor(Color.parseColor("#FFFFFF"))
                    bluetooth_name.setTextColor(Color.parseColor("#1e1e1e"))
                    bluetooth_description.setTextColor(Color.parseColor("#1e1e1e"))
                    bluetooth_distance.setTextColor(Color.parseColor("#1e1e1e"))
                }
            }
        }
    }
}

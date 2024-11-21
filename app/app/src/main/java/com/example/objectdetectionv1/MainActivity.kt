/*
    Hecho por Steven Sequeira y Jefferson Salas
*/

package com.example.objectdetectionv1

import android.content.ActivityNotFoundException
import android.content.Intent
import android.content.res.Configuration
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.view.MenuItem
import android.view.View
import android.widget.FrameLayout
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import com.example.objectdetectionv1.databinding.ActivityMainBinding
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.material.navigation.NavigationView
import com.ingenieriiajhr.jhrCameraX.BitmapResponse
import com.ingenieriiajhr.jhrCameraX.CameraJhr

class MainActivity : AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener {
    // dataclass que almacena todas las sodas disponibles que venden alguna bebida
    data class Soda(val title: String, val snippet: String, val location: LatLng)

    lateinit var binding : ActivityMainBinding
    lateinit var cameraJhr: CameraJhr

    lateinit var classifyTf: ClassifyTf

    companion object{
        const val INPUT_SIZE = 224
    }

    val classes = arrayOf("Coca Cola", "Tropical", "Vaso de vidrio", "No identificado")

    private lateinit var drawer: DrawerLayout
    private lateinit var toggle: ActionBarDrawerToggle

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        classifyTf = ClassifyTf(this)

        //init cameraJHR
        cameraJhr = CameraJhr(this)

        // cambiar el fondo del DrawerLayout por código
        val drawerLayout: DrawerLayout = findViewById(R.id.drawer_layout)
        drawerLayout.setBackgroundColor(resources.getColor(R.color.white))

        val toolbar: androidx.appcompat.widget.Toolbar = findViewById(R.id.toolbar_main)
        setSupportActionBar(toolbar)

        drawer = findViewById(R.id.drawer_layout)
        toggle = ActionBarDrawerToggle(this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close)
        drawer.addDrawerListener(toggle)

        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setHomeButtonEnabled(true)

        val navigationView: NavigationView = findViewById(R.id.nav_view)
        navigationView.setNavigationItemSelectedListener(this)

    }

    override fun onWindowFocusChanged(hasFocus: Boolean) {
        super.onWindowFocusChanged(hasFocus)
        if (!cameraJhr.allpermissionsGranted()) {
            cameraJhr.noPermissions()
        }
    }

    private fun startCameraJhr() {
        var timeRepeat = System.currentTimeMillis()
        cameraJhr.addlistenerBitmap(object : BitmapResponse {
            override fun bitmapReturn(bitmap: Bitmap?) {
                if (bitmap!=null){
                    if(System.currentTimeMillis()>timeRepeat+1000) {
                        classifyImage(bitmap)
                        timeRepeat = System.currentTimeMillis()
                    }
                }
            }
        })
        cameraJhr.initBitmap()
        cameraJhr.initImageProxy()
        cameraJhr.start(1,0,binding.cameraPreview,true,false,true)
    }

    private fun classifyImage(bitmap: Bitmap) {
        //224*224
        val bitmapScale = Bitmap.createScaledBitmap(bitmap, INPUT_SIZE, INPUT_SIZE, false)

        classifyTf.listenerInterpreter(object :ReturnInterpreter {
            override fun classify(confidence: FloatArray, maxConfidence: Int) {
                runOnUiThread {
                    val identifiedObject = classes[maxConfidence]
                    // verifica si el objeto es "no identificado"
                    if (identifiedObject == "No identificado") {
                        binding.txtResult.text = "" // No imprime nada
                    } else {
                        binding.txtResult.text = identifiedObject // imprime el nombre del objeto
                    }
                }
            }
        })

        classifyTf.classify(bitmapScale)
    }


    private fun initializeMap() {
        val mapFragment = supportFragmentManager.findFragmentById(R.id.frame_map) as? SupportMapFragment
            ?: SupportMapFragment.newInstance().also {
                supportFragmentManager.beginTransaction().replace(R.id.frame_map, it).commit()
            }

        mapFragment.getMapAsync { googleMap ->
            // configuración del mapa
            googleMap.uiSettings.isZoomControlsEnabled = true

            val sodas = listOf(
                Soda("Restaurante Institucional TEC", "Bebidas: Agua", LatLng(9.855462180797987, -83.91316681034371)),
                Soda("McDonald's - Dulce Nombre", "Vende: Coca Cola y Agua", LatLng(9.855612374530976, -83.90413011300777)),
                Soda("Soda la Deportiva", "Vende: Coca Cola, Agua y Tropical", LatLng(9.857585086667013, -83.91054864672152)),
                Soda("Soda el Lago", "Vende: Coca Cola, Agua y Tropical", LatLng(9.857585086667013, -83.91054864672152)),
                Soda("Comedor Estudiantil", "Vende: Coca Cola, Agua y Tropical", LatLng(9.854141558346232, -83.90683638843423)),
                Soda("Dian's Café", "Vende: Coca Cola, Agua y Tropical", LatLng(9.856997213065123, -83.90716239833549)),
                Soda("Gibser Ticino", "Vende: Coca Cola, Agua y Tropical", LatLng(9.85843479215909, -83.91302034264235)),
                Soda("La sanwuchera 2", "Vende: Coca Cola, Agua y Tropical", LatLng(9.859949000188466, -83.9122089746811)),
                Soda("Sitio's Ocean", "Vende: Coca Cola, Agua y Tropical", LatLng(9.857359814821686, -83.90446660967372)),
                Soda("La Canela", "Vende: Coca Cola, Agua y Tropical", LatLng(9.85711783357595, -83.90438367742382)),
                Soda("Tequila con Sal", "Vende: Coca Cola, Agua y Tropical", LatLng(9.856291324853357, -83.90462609478236)),
                Soda("King´s Wing´s", "Vende: Coca Cola, Agua y Tropical", LatLng(9.856094528771628, -83.9049163576493)),
                Soda("La Cevechería Grill ", "Vende: Coca Cola, Agua y Tropical", LatLng(9.856088243522604, -83.90449531699595)),
                Soda("Restaurante y Cafetería Nila", "Vende: Coca Cola, Agua y Tropical", LatLng(9.848894646737271, -83.89611377580444)),
                Soda("Bar Versalles Oriental", "Vende: Coca Cola, Agua y Tropical", LatLng(9.860591457173712, -83.90530978446084)),
                Soda("Basho", "Vende: Coca Cola, Agua y Tropical", LatLng(9.858370401403612, -83.91734930427106)),
                Soda("Subway Cartago TEC", "Vende: Coca Cola, Agua y Tropical", LatLng(9.85853755543641, -83.91716178613822)),
                Soda("El Estadio Bar y Restaurante", "Vende: Coca Cola, Agua y Tropical", LatLng(9.861053931658091, -83.91872725978943)),
                Soda("Marisquería y Pescadería La Cambusa", "Vende: Coca Cola, Agua y Tropical", LatLng(9.859992933293903, -83.91749493065045)),
                Soda("Restaurante la Cartaginesa y SPORT BAR", "Vende: Coca Cola, Agua y Tropical", LatLng(9.859764349137762, -83.91912002121552)),
                Soda("Pizzería Villa Italia", "Vende: Coca Cola, Agua y Tropical", LatLng(9.8601310086159, -83.91797740017932)),
            )

            // recorrer la lista de sodas y añadir una marca por cada una
            for (soda in sodas) {
                googleMap.addMarker(
                    MarkerOptions()
                        .position(soda.location)
                        .title(soda.title)
                        .snippet(soda.snippet)
                )
            }

            googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(sodas[0].location, 12f))

            // listener para mostrar InfoWindow al hacer clic en una marca
            googleMap.setOnMarkerClickListener { clickedMarker ->
                clickedMarker.showInfoWindow()
                true
            }

            // listener para abrir Waze cuando se haga clic en el InfoWindow
            googleMap.setOnInfoWindowClickListener { clickedMarker ->
                val clickedLatLng = clickedMarker.position
                openWaze(clickedLatLng.latitude, clickedLatLng.longitude)
            }
        }
    }

    private fun openWaze(lat: Double, lon: Double) {
        val wazeIntentUri = Uri.parse("waze://?ll=$lat,$lon&navigate=yes")
        val intent = Intent(Intent.ACTION_VIEW, wazeIntentUri)
        intent.setPackage("com.waze")
        try {
            startActivity(intent)
        } catch (ex: ActivityNotFoundException) {
            // si Waze no está instalado, abrir Play Store para descargarlo
            val playStoreIntent = Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=com.waze"))
            startActivity(playStoreIntent)
        }
    }

    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.nav_item_home -> {
                // ocultar la vista de todos los frames
                hideAllFrames()
                return true
            }
            R.id.nav_item_one -> {
                // mostrar la cámara en el frame_container
                hideAllFrames()
                findViewById<FrameLayout>(R.id.frame_camera).visibility = View.VISIBLE
                if(!cameraJhr.ifStartCamera) {
                    startCameraJhr()
                }
                return true
            }
            R.id.nav_item_two -> {
                // mostrar el mapa de Google
                hideAllFrames()
                findViewById<FrameLayout>(R.id.frame_map).visibility = View.VISIBLE
                initializeMap()
                return true
            }
        }
        drawer.closeDrawer(GravityCompat.START)
        return true
    }

    private fun hideAllFrames() {
        findViewById<FrameLayout>(R.id.frame_camera).visibility = View.GONE
        findViewById<FrameLayout>(R.id.frame_map).visibility = View.GONE
    }

    override fun onPostCreate(savedInstanceState: Bundle?){
        super.onPostCreate(savedInstanceState)
        toggle.syncState()
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        toggle.onConfigurationChanged(newConfig)

    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if(toggle.onOptionsItemSelected(item)){
            return true
        }
        return super.onOptionsItemSelected(item)
    }

    private fun Float.decimal():String{
        return "%.2f".format(this)
    }
}
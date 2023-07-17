package com.example.farmer.ui

import android.app.Application
import android.content.Context
import android.content.pm.PackageManager
import com.example.farmer.databinding.FragmentSecondBinding

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.example.farmer.Model.Farmer
import com.example.farmer.R
import com.example.farmer.application.FarmerApp
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions

/**
 * A simple [Fragment] subclass as the second destination in the navigation.
 */
class SecondFragment : Fragment(),OnMapReadyCallback, GoogleMap.OnMarkerDragListener{

    private var _binding: FragmentSecondBinding? = null

    private val binding get() = _binding!!
    private lateinit var applicationContext: Context
    private val farmerViewModel: FarmerViewModel by viewModels {
        FarmerViewModelFactory((applicationContext as FarmerApp).repository)
    }

    private val args: SecondFragmentArgs by navArgs()
    private var farmer:Farmer? = null
    private lateinit var nMap: GoogleMap
    private var currentLatLang: LatLng? =null
    private lateinit var fusedLocationClient:FusedLocationProviderClient

    override fun onAttach(context: Context) {
        super.onAttach(context)
        applicationContext = requireContext().applicationContext
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        _binding = FragmentSecondBinding.inflate(inflater, container, false)
        return binding.root

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        farmer = args.farmer
        if (farmer != null){
            binding.deleteButton.visibility = View.VISIBLE
            binding.saveButton.text="Ubah"
            binding.nameEditText.setText(farmer?.name)
            binding.addressEditText.setText(farmer?.address)
            binding.informationEditText.setText(farmer?.information)
        }

        val mapFragment = childFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)
        checkPermission()

        val name = binding.nameEditText.text
        val address = binding.addressEditText.text
        val information = binding.informationEditText.text
        binding.saveButton.setOnClickListener {
            if (name.isEmpty()) {
            Toast.makeText(context,"nama tidak boleh kosong", Toast.LENGTH_SHORT).show()
            }else if (name.isEmpty()) {
                Toast.makeText(context, "Alamat tidak boleh kosong", Toast.LENGTH_SHORT).show()
            }else if (name.isEmpty()) {
                Toast.makeText(context, "information tidak boleh kosong", Toast.LENGTH_SHORT).show()

            }else {
                if (farmer == null) {
                    val farmer = Farmer(0, name.toString(), address.toString(), information.toString(),currentLatLang?.latitude,currentLatLang?.longitude)
                    farmerViewModel.insert(farmer)
                } else {
                    val farmer = Farmer(farmer?.id!!, name.toString(), address.toString(), information.toString(),currentLatLang?.latitude,currentLatLang?.longitude)
                    farmerViewModel.update(farmer)
                }


                findNavController().popBackStack()
     //  findNavController().navigate(R.id.action_SecondFragment_to_FirstFragment)
            }
        }
        binding.deleteButton.setOnClickListener{
            farmer?.let { farmerViewModel.delete(it) }
            findNavController().popBackStack()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onMapReady(googleMap: GoogleMap) {
        nMap=googleMap

        val uiSettings = nMap.uiSettings
        uiSettings.isZoomControlsEnabled =true
        nMap.setOnMarkerDragListener(this)

    }

    override fun onMarkerDrag(p0: Marker) {
    }

    override fun onMarkerDragEnd(marker: Marker) {
        val newPosition = marker.position
        currentLatLang = LatLng(newPosition.latitude,newPosition.longitude)
        Toast.makeText(context,currentLatLang.toString(),Toast.LENGTH_SHORT).show()
    }

    override fun onMarkerDragStart(p0: Marker) {
    }
    private fun checkPermission(){
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(applicationContext)
        if (ContextCompat.checkSelfPermission(
            applicationContext,
            android.Manifest.permission.ACCESS_FINE_LOCATION
            )== PackageManager.PERMISSION_GRANTED
        ){
            getCurrentLocation()
        }else{
            Toast.makeText(applicationContext,"Akses lokasi di tolak",Toast.LENGTH_SHORT).show()
        }

    }
    private fun getCurrentLocation(){

        if (ContextCompat.checkSelfPermission(
                applicationContext,
                android.Manifest.permission.ACCESS_FINE_LOCATION
            )!= PackageManager.PERMISSION_GRANTED
        ){
            return
        }
        fusedLocationClient.lastLocation
            .addOnSuccessListener { location ->
                if (location!= null){
                    var latLang = LatLng(location.latitude,location.longitude)
                    currentLatLang = latLang
                    var title = "Marker"

                    if (farmer != null){
                        title = farmer?.name.toString()
                        val newCurrentLocation = LatLng(farmer?.latitude!!,farmer?.longitude!!)
                        latLang = newCurrentLocation
                    }

                    val markerOptions = MarkerOptions()
                        .position(latLang)
                        .title(title)
                        .draggable(true)
                        .icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_icon_location))
                    nMap.addMarker(markerOptions)
                    nMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLang,15f))
                }
            }

    }
}
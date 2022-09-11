package org.hmispb.patientregistration

import android.content.SharedPreferences
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.LayoutInflater
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.google.gson.Gson
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import org.hmispb.patientregistration.databinding.ActivityExistingUserBinding
import java.util.*

@AndroidEntryPoint
class ExistingUserActivity : AppCompatActivity() {
    private lateinit var patientViewModel: PatientViewModel
    private lateinit var sharedPreferences: SharedPreferences
    lateinit var binding: ActivityExistingUserBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityExistingUserBinding.inflate(layoutInflater)
        setContentView(binding.root)

        sharedPreferences = getSharedPreferences(Utils.OPD_COUNTER, MODE_PRIVATE)
        patientViewModel = ViewModelProvider(this)[PatientViewModel::class.java]
        val loginPreferences = getSharedPreferences(Utils.LOGIN_RESPONSE_PREF, MODE_PRIVATE)

        binding.btnSubmit.setOnClickListener {
            lifecycleScope.launch  {
                if (patientViewModel.searchPatientByCRNumber(binding.crno.text.toString())) {
                    val dateString = sharedPreferences.getString(Utils.PREV_DATE,"")
                    val prevDate = if(dateString.isNullOrEmpty()) Date(1) else Gson().fromJson(dateString,
                        Date::class.java)
                    val currentDate = Date()
                    val hospitalCode = loginPreferences.getString(Utils.HOSPITAL_CODE,"")
                    val currentMonth = currentDate.month+1
                    val currentYear = currentDate.year + 1900
                    val crMiddle = "${if(currentDate.date<10) "0" else ""}${currentDate.date}${if(currentMonth<10) "0" else ""}${currentMonth}${currentYear.toString().substring(2)}"
                    if(prevDate.before(currentDate) && prevDate.day!=currentDate.day) {
                        sharedPreferences.edit()
                            .putInt(Utils.OPD_ID, 1)
                            .putString(Utils.PREV_DATE, Gson().toJson(currentDate))
                            .commit()
                    }
                    val id = sharedPreferences.getInt(Utils.OPD_ID, 1)
                    var opdId = id.toString()
                    if(opdId.length==1) opdId = "0$opdId"
                    if(opdId.length==2) opdId = "0$opdId"
                    sharedPreferences.edit()
                        .putInt(Utils.OPD_ID, id + 1)
                        .commit()
                    val newCr = hospitalCode + crMiddle + opdId
                    val dialogView =
                        LayoutInflater.from(this@ExistingUserActivity)
                            .inflate(R.layout.confirmation_verified_existing_user, null, false)
                    val dialog = AlertDialog.Builder(this@ExistingUserActivity)
                        .setView(dialogView)
                        .setPositiveButton(
                            "Ok"
                        ) { p0, p1 ->
                            p0.cancel()
                            finish()
                        }
                        .create()
                    dialog.setCanceledOnTouchOutside(false)
                    dialog.setOnShowListener {
                        val crNo : TextView? = dialog.findViewById(R.id.new_cr_no)
                        val opdText: TextView? = dialog.findViewById(R.id.new_opd_no)
                        crNo?.text = newCr
                        opdText?.text = opdId
                    }
                    dialog.show()
                } else {
                    Toast.makeText(this@ExistingUserActivity, "Wrong CR number", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }
}
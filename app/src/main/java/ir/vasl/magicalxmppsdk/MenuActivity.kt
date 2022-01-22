package ir.vasl.magicalxmppsdk

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import ir.vasl.magicalxmppsdk.databinding.ActivityMenuBinding

class MenuActivity : AppCompatActivity(), View.OnClickListener {

    private lateinit var binding: ActivityMenuBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMenuBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.appCompatButtonP2p.setOnClickListener(this)
        binding.appCompatButtonMulti.setOnClickListener(this)

    }

    override fun onClick(view: View?) {
        when (view?.id) {
            R.id.appCompatButton_p2p -> {
                startActivity(Intent(this@MenuActivity, P2PActivity::class.java))
            }
            R.id.appCompatButton_multi -> {
                startActivity(Intent(this@MenuActivity, MucActivity::class.java))
            }
            else -> {}
        }
    }
}
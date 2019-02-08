package by.softteco.icotera_test.utils

import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.view.View
import android.widget.Toast
import by.softteco.icotera_test.BuildConfig

fun AppCompatActivity.toast(msg: String) {
    Toast.makeText(this, msg, Toast.LENGTH_LONG).show()
}

fun View.visible() {
    this.visibility = View.VISIBLE
}

fun View.invisible() {
    this.visibility = View.INVISIBLE
}

fun View.gone() {
    this.visibility = View.GONE
}

fun onlyDebugConsume(f: () -> Unit) {
    if (BuildConfig.DEBUG) {
        f()
    }
}

fun log_d(tag: String, msg: String) = onlyDebugConsume { Log.d(tag, msg) }
fun log_i(tag: String, msg: String) = onlyDebugConsume { Log.i(tag, msg) }
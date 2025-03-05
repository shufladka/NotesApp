package by.bsuir.notesapp.toast

import android.content.Context
import android.widget.Toast
import androidx.annotation.VisibleForTesting

interface ToastProxy {

    fun showToast(context: Context, text: String): Toast

    companion object {
        @VisibleForTesting
      var instance: ToastProxy = ToastProxyImpl()
    }
}

class ToastProxyImpl : ToastProxy {
    override fun showToast(context: Context, text: String): Toast {
        val toast = Toast.makeText(context, text, Toast.LENGTH_SHORT)
        toast.show()
        return toast

    }

}

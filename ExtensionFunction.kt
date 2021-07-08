import android.app.Activity
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.content.Intent.*
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Matrix
import android.net.Uri
import android.os.Build
import android.text.SpannableStringBuilder
import android.text.Spanned
import android.text.TextPaint
import android.text.style.ClickableSpan
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.ImageView
import android.widget.Toast
import androidx.annotation.*
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearSmoothScroller
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import java.io.File
import java.io.FileOutputStream
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.*
import kotlin.math.log2
import kotlin.math.pow

object ExtensionFunction {

    fun View.show(): View {
        if (visibility != View.VISIBLE) {
            visibility = View.VISIBLE
        }
        return this
    }

    fun View.remove(): View {
        if (visibility != View.GONE) {
            visibility = View.GONE
        }
        return this
    }

    fun View.hide(): View {
        if (visibility != View.GONE) {
            visibility = View.GONE
        }
        return this
    }

    fun View.toggleVisibility(): View {
        if (visibility == View.VISIBLE) {
            visibility = View.INVISIBLE
        } else {
            visibility = View.INVISIBLE
        }
        return this
    }

    /**
     * Extension method to get a view as bitmap.
     */
    fun View.getBitmap(): Bitmap {
        val bmp = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bmp)
        draw(canvas)
        canvas.save()
        return bmp
    }


    /**
     * Extension method to provide simpler access to {@link View#getResources()#getString(int)}.
     */
    fun View.getString(stringResId: Int): String = resources.getString(stringResId)

    fun View.showKeyboard() {
        val imm = context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        this.requestFocus()
        imm.showSoftInput(this, 0)
    }

    fun View.hideKeyboard(): Boolean {
        try {
            val inputMethodManager =
                context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            return inputMethodManager.hideSoftInputFromWindow(windowToken, 0)
        } catch (ignored: RuntimeException) {
        }
        return false
    }


    // ----------------------------------Context ---------------------------------------------------

    fun Context.getColorCompat(color: Int) = ContextCompat.getColor(this, color)
    fun Context?.toast(text: CharSequence, duration: Int = Toast.LENGTH_LONG) =
        this?.let { Toast.makeText(it, text, duration).show() }

    fun Context?.toast(@StringRes textId: Int, duration: Int = Toast.LENGTH_LONG) =
        this?.let { Toast.makeText(it, textId, duration).show() }

    fun Context.getInteger(@IntegerRes id: Int) = resources.getInteger(id)
    fun Context.getBoolean(@BoolRes id: Int) = resources.getBoolean(id)
    fun Context.getColor(@ColorRes id: Int) = ContextCompat.getColor(this, id)
    fun Context.getDrawable(@DrawableRes id: Int) = ContextCompat.getDrawable(this, id)


    fun Context.share(text: String, subject: String = ""): Boolean {
        val intent = Intent()
        intent.type = "text/plain"
        intent.putExtra(EXTRA_SUBJECT, subject)
        intent.putExtra(EXTRA_TEXT, text)
        try {
            startActivity(createChooser(intent, null))
            return true
        } catch (e: ActivityNotFoundException) {
            return false
        }
    }


    fun Context.makeCall(number: String): Boolean {
        try {
            val intent = Intent(ACTION_CALL, Uri.parse("tel:$number"))
            startActivity(intent)
            return true
        } catch (e: Exception) {
            return false
        }
    }


    //---------------------------------- Fragment -------------------------------------------------

    fun Fragment?.toast(text: CharSequence, duration: Int = Toast.LENGTH_LONG) =
        this?.let { activity.toast(text, duration) }

    fun Fragment?.toast(@StringRes textId: Int, duration: Int = Toast.LENGTH_LONG) =
        this?.let { activity.toast(textId, duration) }


    //------------------------------- Activity ----------------------------------------------------
    fun Activity.hideSoftKeyboard() {
        if (currentFocus != null) {
            val inputMethodManager = getSystemService(
                Context
                    .INPUT_METHOD_SERVICE
            ) as InputMethodManager
            inputMethodManager.hideSoftInputFromWindow(currentFocus!!.windowToken, 0)
        }
    }

    fun String.isPhone(): Boolean {
        val p = "^1([34578])\\d{9}\$".toRegex()
        return matches(p)
    }

    fun String.isNumeric(): Boolean {
        val p = "^[0-9]+$".toRegex()
        return matches(p)
    }

    fun String.equalsIgnoreCase(other: String) =
        this.toLowerCase().contentEquals(other.toLowerCase())


    fun Char.decimalValue(): Int {
        if (!isDigit())
            throw IllegalArgumentException("Out of range")
        return this.toInt() - '0'.toInt()
    }

    inline fun SpannableStringBuilder.withSpan(
        vararg spans: Any,
        action: SpannableStringBuilder.() -> Unit
    ):
            SpannableStringBuilder {
        val from = length
        action()

        for (span in spans) {
            setSpan(span, from, length, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
        }

        return this
    }

    fun Int.twoDigitTime() = if (this < 10) "0" + toString() else toString()
    fun String.dateInFormat(format: String): Date? {
        val dateFormat = SimpleDateFormat(format, Locale.getDefault())
        var parsedDate: Date? = null
        try {
            parsedDate = dateFormat.parse(this)
        } catch (ignored: ParseException) {
            ignored.printStackTrace()
        }
        return parsedDate
    }

    fun getClickableSpan(color: Int, action: (view: View) -> Unit): ClickableSpan {
        return object : ClickableSpan() {
            override fun onClick(view: View) {
                action(view)
            }

            override fun updateDrawState(ds: TextPaint) {
                super.updateDrawState(ds)
                ds.color = color
            }
        }
    }

    //------------------------------------- Image Related --------------------------------------

    fun Bitmap.resize(w: Number, h: Number): Bitmap {
        val width = width
        val height = height
        val scaleWidth = w.toFloat() / width
        val scaleHeight = h.toFloat() / height
        val matrix = Matrix()
        matrix.postScale(scaleWidth, scaleHeight)
        if (width > 0 && height > 0) {
            return Bitmap.createBitmap(this, 0, 0, width, height, matrix, true)
        }
        return this
    }

    fun Bitmap.saveFile(path: String) {
        val f = File(path)
        if (!f.exists()) {
            f.createNewFile()
        }
        val stream = FileOutputStream(f)
        compress(Bitmap.CompressFormat.PNG, 100, stream)
        stream.flush()
        stream.close()
    }


    fun ImageView.loadFromUrl(imageUrl: String) {
        Glide.with(this).load(imageUrl).into(this)
    }


    // ------------------------------------  OS --------------------------------------------

    inline fun aboveApi(api: Int, included: Boolean = false, block: () -> Unit) {
        if (Build.VERSION.SDK_INT > if (included) api - 1 else api) {
            block()
        }
    }

    inline fun belowApi(api: Int, included: Boolean = false, block: () -> Unit) {
        if (Build.VERSION.SDK_INT < if (included) api + 1 else api) {
            block()
        }
    }

    val Long.formatAsFileSize: String
        get() = log2(if (this != 0L) toDouble() else 1.0).toInt().div(10).let {
            val precision = when (it) {
                0 -> 0; 1 -> 1; else -> 2
            }
            val prefix = arrayOf("", "K", "M", "G", "T", "P", "E", "Z", "Y")
            String.format("%.${precision}f ${prefix[it]}B", toDouble() / 2.0.pow(it * 10.0))
        }

    fun RecyclerView.smoothSnapToPosition(position: Int, snapMode: Int = LinearSmoothScroller.SNAP_TO_START) {
        val smoothScroller = object : LinearSmoothScroller(this.context) {
            override fun getVerticalSnapPreference(): Int = snapMode
            override fun getHorizontalSnapPreference(): Int = snapMode
        }
        smoothScroller.targetPosition = position
        layoutManager?.startSmoothScroll(smoothScroller)
    }
}    

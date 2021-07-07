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
import android.transition.Transition
import android.util.Base64.DEFAULT
import android.util.Base64.encodeToString
import android.view.MenuItem
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.ImageView
import android.widget.Toast
import androidx.annotation.*
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.RoundedBitmapDrawableFactory
import androidx.fragment.app.Fragment
import com.bumptech.glide.Glide
import com.bumptech.glide.request.target.SimpleTarget
import com.google.android.material.snackbar.Snackbar
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.security.MessageDigest
import java.security.NoSuchAlgorithmException
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.*

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
        if (visibility != View.INVISIBLE) {
            visibility = View.INVISIBLE
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

    fun View.snack(message: String, length: Int = Snackbar.LENGTH_LONG) = snack(message, length) {}


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
            val inputMethodManager = context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
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

    fun Context.browse(url: String, newTask: Boolean = false): Boolean {
        try {
            val intent = intent(ACTION_VIEW) {
                data = Uri.parse(url)
                if (newTask) addFlags(FLAG_ACTIVITY_NEW_TASK)
            }
            startActivity(intent)
            return true
        } catch (e: Exception) {
            return false
        }
    }

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

    fun Context.email(email: String, subject: String = "", text: String = ""): Boolean {
        val intent = intent(ACTION_SENDTO) {
            data = Uri.parse("mailto:")
            putExtra(EXTRA_EMAIL, arrayOf(email))
            if (subject.isNotBlank()) putExtra(EXTRA_SUBJECT, subject)
            if (text.isNotBlank()) putExtra(EXTRA_TEXT, text)
        }
        if (intent.resolveActivity(packageManager) != null) {
            startActivity(intent)
            return true
        }
        return false
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

    fun Context.sendSms(number: String, text: String = ""): Boolean {
        try {
            val intent = intent(ACTION_VIEW, Uri.parse("sms:$number")) {
                putExtra("sms_body", text)
            }
            startActivity(intent)
            return true
        } catch (e: Exception) {
            return false
        }
    }

    fun Context.rate(): Boolean =
        browse("market://details?id=$packageName") or browse("http://play.google.com/store/apps/details?id=$packageName")


    //---------------------------------- Fragment -------------------------------------------------

    fun Fragment?.toast(text: CharSequence, duration: Int = Toast.LENGTH_LONG) =
        this?.let { activity.toast(text, duration) }

    fun Fragment?.toast(@StringRes textId: Int, duration: Int = Toast.LENGTH_LONG) =
        this?.let { activity.toast(textId, duration) }

    fun SupportFragment?.toast(text: CharSequence, duration: Int = Toast.LENGTH_LONG) =
        this?.let { activity.toast(text, duration) }

    fun SupportFragment?.toast(@StringRes textId: Int, duration: Int = Toast.LENGTH_LONG) =
        this?.let { activity.toast(textId, duration) }

    fun Fragment.browse(url: String, newTask: Boolean = false) = activity.browse(url, newTask)
    fun SupportFragment.browse(url: String, newTask: Boolean = false) = activity.browse(url, newTask)
    fun Fragment.share(text: String, subject: String = "") = activity.share(text, subject)
    fun SupportFragment.share(text: String, subject: String = "") = activity.share(text, subject)
    fun Fragment.email(email: String, subject: String = "", text: String = "") = activity.email(email, subject, text)
    fun SupportFragment.email(email: String, subject: String = "", text: String = "") =
        activity.email(email, subject, text)

    fun Fragment.makeCall(number: String) = activity.makeCall(number)
    fun SupportFragment.makeCall(number: String) = activity.makeCall(number)
    fun Fragment.sendSms(number: String, text: String = "") = activity.sendSms(number, text)
    fun SupportFragment.sendSms(number: String, text: String = "") = activity.sendSms(number, text)
    fun Fragment.hideSoftKeyboard() {
        activity?.hideSoftKeyboard()
    }


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


    // ------------------------------ String -------------------------------------------------------
    fun String.isPhone(): Boolean {
        val p = "^1([34578])\\d{9}\$".toRegex()
        return matches(p)
    }

    fun String.sha1() = encrypt(this, "SHA-1")
    fun String.md5() = encrypt(this, "MD5")
    fun String.toast(isShortToast: Boolean = true) = toast(this, isShortToast)
    fun String.isPhone(): Boolean {
        val p = "^1([34578])\\d{9}\$".toRegex()
        return matches(p)
    }

    fun String.isNumeric(): Boolean {
        val p = "^[0-9]+$".toRegex()
        return matches(p)
    }

    fun String.equalsIgnoreCase(other: String) = this.toLowerCase().contentEquals(other.toLowerCase())
    private fun encrypt(string: String?, type: String): String {
        if (string.isNullOrEmpty()) {
            return ""
        }
        val md5: MessageDigest
        return try {
            md5 = MessageDigest.getInstance(type)
            val bytes = md5.digest(string!!.toByteArray())
            bytes2Hex(bytes)
        } catch (e: NoSuchAlgorithmException) {
            ""
        }
    }

    fun Char.decimalValue(): Int {
        if (!isDigit())
            throw IllegalArgumentException("Out of range")
        return this.toInt() - '0'.toInt()
    }

    inline fun SpannableStringBuilder.withSpan(vararg spans: Any, action: SpannableStringBuilder.() -> Unit):
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
    fun Bitmap.toBase64(): String {
        var result = ""
        val baos = ByteArrayOutputStream()
        try {
            compress(Bitmap.CompressFormat.JPEG, 100, baos)
            baos.flush()
            baos.close()
            val bitmapBytes = baos.toByteArray()
            result = Base64.encodeToString(bitmapBytes, Base64.DEFAULT)
        } catch (e: IOException) {
            e.printStackTrace()
        } finally {
            try {
                baos.flush()
                baos.close()
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }
        return result
    }
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

    fun MenuItem.loadIconFromUrl(context: Context, imageUrl: String) {
        Glide.with(context).asBitmap()
            .load(imageUrl)
            .into(object : SimpleTarget<Bitmap>(100, 100) {
                override fun onResourceReady(resource: Bitmap?, transition: Transition<in Bitmap>?) {
                    val circularIcon = RoundedBitmapDrawableFactory.create(context.resources, resource)
                    circularIcon.isCircular = true
                    icon = circularIcon
                }
            })
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

     fun RecyclerView.smoothSnapToPosition(position: Int, snapMode: Int = LinearSmoothScroller.SNAP_TO_START) {
        val smoothScroller = object : LinearSmoothScroller(this.context) {
            override fun getVerticalSnapPreference(): Int = snapMode
            override fun getHorizontalSnapPreference(): Int = snapMode
        }
        smoothScroller.targetPosition = position
        layoutManager?.startSmoothScroll(smoothScroller)
    }

}    

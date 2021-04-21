object ExtensionFunction {

    fun View.show(): View {
        if (visibility != View.VISIBLE) {
            visibility = View.VISIBLE
        }
        return this
    }

    fun View.hide() {
        visibility = View.GONE
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

    fun Fragment?.toast(text: CharSequence, duration: Int = Toast.LENGTH_LONG) = this?.let { activity.toast(text, duration) }
    fun Fragment?.toast(@StringRes textId: Int, duration: Int = Toast.LENGTH_LONG) = this?.let { activity.toast(textId, duration) }
    fun SupportFragment?.toast(text: CharSequence, duration: Int = Toast.LENGTH_LONG) = this?.let { activity.toast(text, duration) }
    fun SupportFragment?.toast(@StringRes textId: Int, duration: Int = Toast.LENGTH_LONG) = this?.let { activity.toast(textId, duration) }
    fun Fragment.browse(url: String, newTask: Boolean = false) = activity.browse(url, newTask)
    fun SupportFragment.browse(url: String, newTask: Boolean = false) = activity.browse(url, newTask)
    fun Fragment.share(text: String, subject: String = "") = activity.share(text, subject)
    fun SupportFragment.share(text: String, subject: String = "") = activity.share(text, subject)
    fun Fragment.email(email: String, subject: String = "", text: String = "") = activity.email(email, subject, text)
    fun SupportFragment.email(email: String, subject: String = "", text: String = "") = activity.email(email, subject, text)
    fun Fragment.makeCall(number: String) = activity.makeCall(number)
    fun SupportFragment.makeCall(number: String) = activity.makeCall(number)
    fun Fragment.sendSms(number: String, text: String = "") = activity.sendSms(number, text)
    fun SupportFragment.sendSms(number: String, text: String = "") = activity.sendSms(number, text)
    fun Fragment.hideSoftKeyboard() {
        activity?.hideSoftKeyboard()
    }

}    
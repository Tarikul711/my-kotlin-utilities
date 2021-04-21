object ExtensionFunction {

    fun View.show() : View {
        if (visibility != View.VISIBLE) {
            visibility = View.VISIBLE
        }
        return this
    }

    fun View.hide() {
        visibility = View.GONE
    }

    fun View.remove() : View {
        if (visibility != View.GONE) {
            visibility = View.GONE
        }
        return this
    }

    fun View.hide() : View {
        if (visibility != View.INVISIBLE) {
            visibility = View.INVISIBLE
        }
        return this
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
            val inputMethodManager = context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            return inputMethodManager.hideSoftInputFromWindow(windowToken, 0)
        } catch (ignored: RuntimeException) {
        }
        return false
    }
}    
package su.rbv.ecwid_sklad.utils

import android.text.InputFilter
import android.text.Spanned
import java.util.regex.Matcher
import java.util.regex.Pattern

/** Decimal filter for text values: DDDDD{,|.}DD */
class DecimalDigitsInputFilter : InputFilter {

    private val pattern = Pattern.compile("^[0-9]+((((\\.|,)[0-9]{0,2})?)|(\\.|,)?)\$")

    override fun filter(
        source: CharSequence,
        start: Int,
        end: Int,
        dest: Spanned,
        dstart: Int,
        dend: Int
    ): CharSequence? {
        val matcher: Matcher = pattern.matcher(dest.toString() + source)
        return if (matcher.matches()) null else ""
    }

}
package su.rbv.ecwid_sklad.data

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.lang.NumberFormatException

@Entity
class Item {
    @PrimaryKey (autoGenerate = true)
    var id: Long = 0

    var name: String? = null

    var price: Double = .0

    /** convert string price value to double price field */
    var priceStr: String?
        get() = String.format("%.02f", price)
        set(value) {
            price = try {
                value?.replace(",", ".")?.toDouble() ?: .0
            } catch (e: NumberFormatException) {
                .0
            }
        }

    /** JPEG compressed image */
    var image: ByteArray? = null
}

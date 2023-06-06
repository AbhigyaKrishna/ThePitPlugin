package me.abhigya.pit.database.binding

import me.abhigya.pit.model.Balance
import me.abhigya.pit.model.toBalance
import org.jooq.Converter

class BalanceConverter : Converter<Double, Balance> {
    override fun from(databaseObject: Double): Balance {
        return databaseObject.toBalance()
    }

    override fun to(userObject: Balance): Double {
        return userObject.toDouble()
    }

    override fun fromType(): Class<Double> = Double::class.java

    override fun toType(): Class<Balance> = Balance::class.java

}
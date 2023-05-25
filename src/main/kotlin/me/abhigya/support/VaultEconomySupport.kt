package me.abhigya.support

import me.abhigya.pit.model.PitPlayer.Companion.toPitPlayer
import me.abhigya.pit.model.toBalance
import net.milkbowl.vault.economy.Economy
import net.milkbowl.vault.economy.EconomyResponse
import org.bukkit.OfflinePlayer
import org.bukkit.entity.Player
import org.bukkit.plugin.Plugin
import toothpick.InjectConstructor

@InjectConstructor
class VaultEconomySupport(
    private val plugin: Plugin
) : Economy {

    companion object {
        val TRANSACTION_ERROR_UNIMPLEMENTED: EconomyResponse = EconomyResponse(0.0, 0.0, EconomyResponse.ResponseType.NOT_IMPLEMENTED, "unimplemented")
        val TRANSACTION_ERROR_PLAYER_NOT_ONLINE: EconomyResponse = EconomyResponse(0.0, 0.0, EconomyResponse.ResponseType.FAILURE, "player is not online")
        val TRANSACTION_ERROR_INSUFFICIENT_BALANCE: (Double) -> EconomyResponse = {
            EconomyResponse(0.0, it, EconomyResponse.ResponseType.FAILURE, "insufficient balance")
        }
    }

    override fun isEnabled(): Boolean = true

    override fun getName(): String = "Pit Economy"

    override fun hasBankSupport(): Boolean = false

    override fun fractionalDigits(): Int = 1

    override fun format(amount: Double): String {
        return amount.toBalance().toString()
    }

    override fun currencyNamePlural(): String {
        TODO("Not yet implemented")
    }

    override fun currencyNameSingular(): String {
        TODO("Not yet implemented")
    }

    override fun hasAccount(playerName: String): Boolean {
        return getPlayer(playerName) != null
    }

    override fun hasAccount(player: OfflinePlayer): Boolean = player.hasPlayedBefore()

    override fun hasAccount(playerName: String, worldName: String): Boolean = hasAccount(playerName)

    override fun hasAccount(player: OfflinePlayer, worldName: String): Boolean = hasAccount(player)

    override fun getBalance(playerName: String): Double {
        return getBalance(getPlayer(playerName) ?: return 0.0)
    }

    override fun getBalance(player: OfflinePlayer): Double {
        return if (player.isOnline) {
            player.player?.toPitPlayer()?.balance?.toDouble() ?: 0.0
        } else {
            0.0
        }
    }

    override fun getBalance(playerName: String, worldName: String): Double = getBalance(playerName)

    override fun getBalance(player: OfflinePlayer, worldName: String): Double = getBalance(player)

    override fun has(playerName: String, amount: Double): Boolean {
        return has(getPlayer(playerName) ?: return false, amount)
    }

    override fun has(player: OfflinePlayer, amount: Double): Boolean {
        return if (player.isOnline) {
            player.player?.toPitPlayer()?.balance?.has(amount) ?: false
        } else {
            false
        }
    }

    override fun has(playerName: String, worldName: String, amount: Double): Boolean = has(playerName, amount)

    override fun has(player: OfflinePlayer, worldName: String, amount: Double): Boolean = has(player, amount)

    override fun withdrawPlayer(playerName: String, amount: Double): EconomyResponse {
        return withdrawPlayer(getPlayer(playerName) ?: return TRANSACTION_ERROR_PLAYER_NOT_ONLINE, amount)
    }

    override fun withdrawPlayer(player: OfflinePlayer, amount: Double): EconomyResponse {
        return if (player.isOnline) {
            val balance = player.player?.toPitPlayer()?.balance ?: return TRANSACTION_ERROR_PLAYER_NOT_ONLINE
            if (balance.has(amount)) {
                balance -= amount
                EconomyResponse(amount, balance.toDouble(), EconomyResponse.ResponseType.SUCCESS, null)
            } else {
                TRANSACTION_ERROR_INSUFFICIENT_BALANCE(balance.toDouble())
            }
        } else {
            TRANSACTION_ERROR_PLAYER_NOT_ONLINE
        }
    }

    override fun withdrawPlayer(playerName: String, worldName: String, amount: Double): EconomyResponse = withdrawPlayer(playerName, amount)

    override fun withdrawPlayer(player: OfflinePlayer, worldName: String, amount: Double): EconomyResponse = withdrawPlayer(player, amount)

    override fun depositPlayer(playerName: String, amount: Double): EconomyResponse {
        return depositPlayer(getPlayer(playerName) ?: return TRANSACTION_ERROR_PLAYER_NOT_ONLINE, amount)
    }

    override fun depositPlayer(player: OfflinePlayer, amount: Double): EconomyResponse {
        return if (player.isOnline) {
            val balance = player.player?.toPitPlayer()?.balance ?: return TRANSACTION_ERROR_PLAYER_NOT_ONLINE
            balance += amount
            EconomyResponse(amount, balance.toDouble(), EconomyResponse.ResponseType.SUCCESS, null)
        } else {
            TRANSACTION_ERROR_PLAYER_NOT_ONLINE
        }
    }

    override fun depositPlayer(playerName: String, worldName: String, amount: Double): EconomyResponse = depositPlayer(playerName, amount)

    override fun depositPlayer(player: OfflinePlayer, worldName: String, amount: Double): EconomyResponse = depositPlayer(player, amount)

    override fun createBank(name: String, playerName: String): EconomyResponse = TRANSACTION_ERROR_UNIMPLEMENTED

    override fun createBank(name: String, player: OfflinePlayer): EconomyResponse = TRANSACTION_ERROR_UNIMPLEMENTED

    override fun deleteBank(playerName: String): EconomyResponse = TRANSACTION_ERROR_UNIMPLEMENTED

    override fun bankBalance(playerName: String): EconomyResponse = TRANSACTION_ERROR_UNIMPLEMENTED

    override fun bankHas(playerName: String, amount: Double): EconomyResponse = TRANSACTION_ERROR_UNIMPLEMENTED

    override fun bankWithdraw(playerName: String, amount: Double): EconomyResponse = TRANSACTION_ERROR_UNIMPLEMENTED

    override fun bankDeposit(playerName: String, amount: Double): EconomyResponse = TRANSACTION_ERROR_UNIMPLEMENTED

    override fun isBankOwner(name: String, playerName: String): EconomyResponse = TRANSACTION_ERROR_UNIMPLEMENTED

    override fun isBankOwner(name: String, player: OfflinePlayer): EconomyResponse = TRANSACTION_ERROR_UNIMPLEMENTED

    override fun isBankMember(name: String, playerName: String): EconomyResponse = TRANSACTION_ERROR_UNIMPLEMENTED

    override fun isBankMember(name: String, player: OfflinePlayer): EconomyResponse = TRANSACTION_ERROR_UNIMPLEMENTED

    override fun getBanks(): List<String> = emptyList()

    override fun createPlayerAccount(playerName: String): Boolean = true

    override fun createPlayerAccount(player: OfflinePlayer): Boolean = true

    override fun createPlayerAccount(playerName: String, worldName: String): Boolean = true

    override fun createPlayerAccount(player: OfflinePlayer, worldName: String): Boolean = true

    private fun getPlayer(playerName: String): Player? {
        return plugin.server.getPlayer(playerName)
    }

}
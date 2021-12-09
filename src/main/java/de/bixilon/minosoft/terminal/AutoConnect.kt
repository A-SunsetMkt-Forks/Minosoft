package de.bixilon.minosoft.terminal

import de.bixilon.minosoft.config.profile.profiles.account.AccountProfileManager
import de.bixilon.minosoft.data.accounts.Account
import de.bixilon.minosoft.data.registries.versions.Version
import de.bixilon.minosoft.data.registries.versions.Versions
import de.bixilon.minosoft.modding.event.events.connection.play.PlayConnectionStateChangeEvent
import de.bixilon.minosoft.modding.event.events.connection.status.ServerStatusReceiveEvent
import de.bixilon.minosoft.modding.event.invoker.CallbackEventInvoker
import de.bixilon.minosoft.protocol.network.connection.play.PlayConnection
import de.bixilon.minosoft.protocol.network.connection.play.PlayConnectionStates.Companion.disconnected
import de.bixilon.minosoft.protocol.network.connection.status.StatusConnection
import de.bixilon.minosoft.util.DNSUtil
import de.bixilon.minosoft.util.ServerAddress
import de.bixilon.minosoft.util.logging.Log
import de.bixilon.minosoft.util.logging.LogLevels
import de.bixilon.minosoft.util.logging.LogMessageType
import kotlin.system.exitProcess

object AutoConnect {


    private fun autoConnect(address: ServerAddress, version: Version, account: Account) {
        val connection = PlayConnection(
            address = address,
            account = account,
            version = version,
        )
        connection.registerEvent(CallbackEventInvoker.of<PlayConnectionStateChangeEvent> {
            if (it.state.disconnected && RunConfiguration.DISABLE_EROS) {
                Log.log(LogMessageType.AUTO_CONNECT, LogLevels.INFO) { "Disconnected from server, exiting..." }
                exitProcess(0)
            }
        })
        Log.log(LogMessageType.AUTO_CONNECT, LogLevels.INFO) { "Connecting to $address, with version $version using account $account..." }
        connection.connect()
    }

    fun autoConnect(connectString: String) {
        // ToDo: Show those connections in eros
        val split = connectString.split(',')
        val address = split[0]
        val version = Versions.getVersionByName(split.getOrNull(1) ?: "automatic") ?: throw IllegalArgumentException("Auto connect: Version not found!")
        val accountProfile = AccountProfileManager.selected
        val account = accountProfile.entries[split.getOrNull(2)] ?: accountProfile.selected ?: throw RuntimeException("Auto connect: Account not found! Have you started normal before or added an account?")

        if (version == Versions.AUTOMATIC_VERSION) {
            Log.log(LogMessageType.AUTO_CONNECT, LogLevels.INFO) { "Pinging server to get version..." }
            val ping = StatusConnection(address)
            ping.ping()
            ping.registerEvent(CallbackEventInvoker.of<ServerStatusReceiveEvent> {
                autoConnect(ping.realAddress!!, ping.serverVersion ?: throw IllegalArgumentException("Could not determinate server's version!"), account)
            })
            return
        }

        autoConnect(DNSUtil.getServerAddress(address), version, account)
    }
}
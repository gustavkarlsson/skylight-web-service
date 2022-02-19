package se.gustavkarlsson.skylight

import io.kotest.core.config.AbstractProjectConfig
import io.kotest.core.spec.IsolationMode

object GlobalConfig : AbstractProjectConfig() {
    override val isolationMode = IsolationMode.InstancePerLeaf
}

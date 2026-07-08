package net.irext.ircontrol.compose.data

import com.activeandroid.query.Select
import net.irext.ircontrol.compose.bean.RemoteControl

class RemoteControlRepository {
    fun list(from: Int, count: Int): List<RemoteControl> = Select()
        .from(RemoteControl::class.java)
        .orderBy("id DESC")
        .offset(from)
        .limit(count)
        .execute()

    fun get(id: Long): RemoteControl? = Select()
        .from(RemoteControl::class.java)
        .where("id = ?", id)
        .execute<RemoteControl>()
        ?.firstOrNull()

    fun save(remote: RemoteControl): Long = remote.save()
}

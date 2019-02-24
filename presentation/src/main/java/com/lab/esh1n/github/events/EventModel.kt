package com.lab.esh1n.github.events

import java.util.*

/**
 * Created by esh1n on 3/16/18.
 */

data class EventModel(val id: Long,
                      val type: String,
                      val repositoryName: String,
                      val actorAvatar: String,
                      val actorName: String?,
                      val createdDate: Date)
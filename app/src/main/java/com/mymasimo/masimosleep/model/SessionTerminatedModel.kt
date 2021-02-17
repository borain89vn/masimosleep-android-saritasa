package com.mymasimo.masimosleep.model

abstract class SessionTerminatedModel(val cause: SessionTerminatedCause?)

class EndedModel(val sessionId: Long, val night: Int, cause: SessionTerminatedCause?) : SessionTerminatedModel(cause)
class CanceledModel(cause: SessionTerminatedCause?) : SessionTerminatedModel(cause)

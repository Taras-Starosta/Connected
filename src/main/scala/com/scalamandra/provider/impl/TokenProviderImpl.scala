package com.scalamandra.provider.impl

import com.scalamandra.provider.TokenProvider

import java.util.UUID

class TokenProviderImpl extends TokenProvider {

  override def generateToken: String =
    UUID.randomUUID.toString

}
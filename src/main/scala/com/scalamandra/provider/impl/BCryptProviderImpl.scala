package com.scalamandra.provider.impl

import com.github.t3hnar.bcrypt._
import com.scalamandra.config.BCryptConfig
import com.scalamandra.provider.BCryptProvider

class BCryptProviderImpl(bCryptConfig: BCryptConfig) extends BCryptProvider {

  override def encrypt(password: String): String =
    password.bcryptBounded(bCryptConfig.rounds)

  override def compare(open: String, hashed: String): Boolean =
    open.isBcryptedBounded(hashed)

}
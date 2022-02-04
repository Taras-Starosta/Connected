package com.scalamandra.provider

trait BCryptProvider {

  def encrypt(password: String): String

  def compare(open: String, hashed: String): Boolean

}
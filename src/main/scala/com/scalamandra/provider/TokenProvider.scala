package com.scalamandra.provider

trait TokenProvider {

  def generateToken: String

}
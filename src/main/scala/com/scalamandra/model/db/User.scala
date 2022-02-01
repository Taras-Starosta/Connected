package com.scalamandra.model.db

case class User(
                 id: Long,
                 nickname: String,
                 email: String,
                 password: String,
                 avatarUrl: Option[String],
                 active: Boolean,
               )
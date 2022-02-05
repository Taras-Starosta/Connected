package com.scalamandra.model.dto.auth

case class ActivationRequest(
                              token: String,
                              userId: Long,
                            )
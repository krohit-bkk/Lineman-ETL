package com.lineman.utils

import org.slf4j.LoggerFactory
import org.slf4j.Logger

trait LoggingUtils {
  @transient val logger : Logger = LoggerFactory.getLogger(getClass)
}
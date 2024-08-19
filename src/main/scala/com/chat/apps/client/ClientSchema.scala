package com.chat.apps.client

import com.chat.core.ViewerSchema
import javax.inject.Inject
import com.chat.core.AbstractSchema
import java.util
import scala.concurrent.ExecutionContext

class ClientSchema @Inject() (schemas: util.Set[AbstractSchema[ClientCtx]])(implicit ec: ExecutionContext)
    extends ViewerSchema[ClientCtx](schemas.toArray(new Array[AbstractSchema[ClientCtx]](0)).toSeq) {}

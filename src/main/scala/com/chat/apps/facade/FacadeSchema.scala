package com.chat.apps.facade

import com.chat.core.ViewerSchema
import javax.inject.Inject
import com.chat.core.AbstractSchema
import java.util
import scala.concurrent.ExecutionContext

class FacadeSchema @Inject() (schemas: util.Set[AbstractSchema[FacadeCtx]])(implicit ec: ExecutionContext)
    extends ViewerSchema[FacadeCtx](schemas.toArray(new Array[AbstractSchema[FacadeCtx]](0)).toSeq) {}

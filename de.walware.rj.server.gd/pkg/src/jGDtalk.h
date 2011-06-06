#ifndef __JGD_TALK_H__
#define __JGD_TALK_H__

#ifdef HAVE_CONFIG_H
# include <config.h>
#endif

#include "javaGD.h"

extern void setupJavaGDfunctions(NewDevDesc *dd);

Rboolean newJavaGD_NewDevice(NewDevDesc *dd, newJavaGDDesc *xd,
		char *dsp, double w, double h);
void newJavaGD_Open(NewDevDesc *dd);

#endif

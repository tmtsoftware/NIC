/******************************************************************************
 ****         D A O   I N S T R U M E N T A T I O N   G R O U P           *****
 *
 * (c) 2007-2018                          (c) 2007-2018
 * National Research Council              Conseil national de recherches
 * Ottawa, Canada, K1A 0R6                Ottawa, Canada, K1A 0R6
 * All rights reserved                    Tous droits reserves
 *                  
 * NRC disclaims any warranties,          Le CNRC denie toute garantie 
 * expressed, implied, or statutory, of   enoncee, implicite ou legale, de
 * any kind with respect to the soft-     quelque nature que se soit, concer-
 * ware, including without limitation     nant le logiciel, y compris sans 
 * any warranty of merchantability or     restriction toute garantie de valeur
 * fitness for a particular purpose.      marchande u de pertinence pour un
 * NRC shall not be liable in any event   usage particulier. Le CNRC ne pourra 
 * for any damages, whether direct or     en aucun cas etre tenu responsable
 * indirect, special or general, conse-   de tout dommage, direct ou indirect,
 * quential or incidental, arising from   particulier ou general, accessoire 
 * the use of the software.               ou fortuit, resultant de l'utili-
 *                                        sation du logiciel. 
 *
 *****************************************************************************/
/*!
 ******************************************************************************
 * \file templateA.c
 * \brief source code brief 
 * \n\n
 *
 * <b> Implementation Details: </b>\n\n
 * source code description
 *
 * <hr>
 ******************************************************************************
 */

/*-----------------------------------------------------------------------------
 * Defines
 *---------------------------------------------------------------------------*/

/*! \brief local define description */
#define TEMPLATE_LOCAL_DEF 11 


/*-----------------------------------------------------------------------------
 * Macros
 *---------------------------------------------------------------------------*/

/*
 ******************************************************************************
 * TEMPLATE_LOCAL_MACRO()
 ******************************************************************************
 *//*!
 * \brief
 * local macro brief description
 *
 * <b> Implementation Details: </b>\n\n
 * local macro description
 *
 * \param[in] a (char *) input param description
 *
 * \callgraph
 ******************************************************************************
 */
#define TEMPLATE_LOCAL_MACRO( a )                                              \
{                                                                             \
    /* implementation */                                                      \
}


/*-----------------------------------------------------------------------------
 * Includes
 *---------------------------------------------------------------------------*/

#include <errno.h>

#include "templateA.h"

/*-----------------------------------------------------------------------------
 * Typedefs
 *---------------------------------------------------------------------------*/


/*! \brief local enum description */
typedef enum {
    localEnum_NUM1 = 1, /*!< \brief local enum value 1 description */
    localEnum_NUM2 = 2  /*!< \brief local enum value 2 description */
} localEnum_t;

typedef void * local_t; /*!< \brief local typedef description */

/*-----------------------------------------------------------------------------
 * Local Variables Initialization
 *---------------------------------------------------------------------------*/

static float localVar = 0.0; /*!< \brief local global variable desc */

/*-----------------------------------------------------------------------------
 * Exported Variables Initialization
 *---------------------------------------------------------------------------*/

int templateExportVar = 0; /*!< \brief exported global var desc */

/*-----------------------------------------------------------------------------
 * Local Function Declarations 
 *---------------------------------------------------------------------------*/

static float localFunc( float a );

/*-----------------------------------------------------------------------------
 * Exported Function Definitions 
 *---------------------------------------------------------------------------*/

/*
 ******************************************************************************
 * templateExportFunc()
 ******************************************************************************
 *//*!
 * \brief
 * exported function brief description
 *
 * <b> Implementation Details: </b>\n\n
 * exported function description
 *
 * \param[in] a (double) input param description
 * \param[in,out] b (int *) modified param description
 * \param[out] c (char *) output param description 
 *
 * \return (int) return value general description
 * \retval 0 return value case 0 description
 * \retval 1 return value case 1 description
 *
 * \callgraph
 ******************************************************************************
 */
int templateExportFunc
( 
    double a, 
    int * b, 
    char * c 
)
{
    float rv = localFunc( localVar );
    
    /* implementation */
    return (int) rv;
}

/*-----------------------------------------------------------------------------
 * Local Function Definitions 
 *---------------------------------------------------------------------------*/

/*
 ******************************************************************************
 * localFunc()
  *****************************************************************************
 *//*!
 * \brief
 * local function brief description
 *
 * <b> Implementation Details: </b>\n\n
 * local function description
 *
 * \param[in] a (float) input param description
 *
 * \return (float) return value general description
 * \retval >= 0 return value case 0 description
 * \retval < 0 return value case 1 description
 *
 * \callgraph
 ******************************************************************************
 */
float localFunc
( 
    float a 
)
{
    /* implementation */
    return 0.0;
}

#ifdef COMP_TEST_MAIN
/*
 ******************************************************************************
 * main()
  *****************************************************************************
 *//*!
 * \brief
 * cmd line test program brief description
 *
 * <b> Implementation Details: </b>\n\n
 * cmd line test program description
 *
 * \param[in] argc (int) mumber of command line args
 * \param[in] argv (const char * []) List of command line args
 *
 * \return (int) return value general description
 * \retval >= 0 return value case 0 description
 * \retval < 0 return value case 1 description
 *
 * \callgraph
 ******************************************************************************
 */
int main
(
    int argc,     
    const char * argv[] 
)
{
    /* implementation */
    return 0;
}
#endif

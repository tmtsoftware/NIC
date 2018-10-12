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
 * \file templateB.cpp
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

/*-----------------------------------------------------------------------------
 * Macros
 *---------------------------------------------------------------------------*/

/*-----------------------------------------------------------------------------
 * Includes
 *---------------------------------------------------------------------------*/

#include "templateB.hpp"

/*-----------------------------------------------------------------------------
 * Typedefs
 *---------------------------------------------------------------------------*/

/*-----------------------------------------------------------------------------
 * Local Variables
 *---------------------------------------------------------------------------*/

/*-----------------------------------------------------------------------------
 * Exported Variables 
 *---------------------------------------------------------------------------*/

/*-----------------------------------------------------------------------------
 * Static Class Attributes Initialization
 *---------------------------------------------------------------------------*/

/*! \brief class static attribute description */
int TemplateClass::attributeStatic = 1; 

/*-----------------------------------------------------------------------------
 * Local Function Declarations 
 *---------------------------------------------------------------------------*/

/*-----------------------------------------------------------------------------
 * Class Method Definitions
 *---------------------------------------------------------------------------*/

/*
 ##############################################################################
 # TemplateBaseClass
 ##############################################################################
 */

/*
 ******************************************************************************
 * TemplateBaseClass::TemplateBaseClass() 
 ******************************************************************************
 *//*!
 * \brief
 * base constructor brief
 *
 * <b> Implementation Details: </b>\n\n
 * base constructor description
 *
 * \param - none -
 *
 * \return N/A
 *
 * \callgraph
 ******************************************************************************
 */
TemplateBaseClass::TemplateBaseClass( void ) 
{
    attributeBase = 0;
    /* implementation */
    return;
}

/*
 ******************************************************************************
 * TemplateBaseClass::~TemplateBaseClass() 
 ******************************************************************************
 *//*!
 * \brief
 * base deconstructor brief
 *
 * <b> Implementation Details: </b>\n\n
 * base deconstructor description
 *
 * \param - none -
 *
 * \return N/A
 *
 * \callgraph
 ******************************************************************************
 */
TemplateBaseClass::~TemplateBaseClass( void ) 
{
    /* implementation */
    return;
}

/*
 ******************************************************************************
 * TemplateBaseClass::publicBaseMethod()
 ******************************************************************************
 *//*!
 * \brief
 * base public method brief
 *
 * <b> Implementation Details: </b>\n\n
 * base public method description
 *
 * \param[in] a (double) input param description
 * \param[in,out] b (int *) modified param description
 * \param[out] c (char *) output param description 
 *
 * \return - none -
 *
 * \callgraph
 ******************************************************************************
 */
void TemplateBaseClass::publicBaseMethod
( 
    double a, 
    int * b, 
    char * c 
)
{
    /* implementation */
    return;
}

/*
 ******************************************************************************
 * TemplateBaseClass::privateBaseMethod()
 ******************************************************************************
 *//*!
 * \brief
 * base private method brief
 *
 * <b> Implementation Details: </b>\n\n
 * base private method description
 *
 * \param[in] a (int) input param description
 * \param[in] b (short int) modified param description
 *
 * \return (double *) return value description
 *
 * \callgraph
 ******************************************************************************
 */
double * TemplateBaseClass::privateBaseMethod
( 
    int a, 
    short int b 
)
{
    /* implementation */
    return NULL;
}

/*
 ##############################################################################
 # TemplateClass
 ##############################################################################
 */

/*
 ******************************************************************************
 * TemplateClass::TemplateClass()
 ******************************************************************************
 *//*!
 * \brief
 * constructor brief
 *
 * <b> Implementation Details: </b>\n\n
 * constructor description
 *
 * \param[in] a (int) input param description
 *
 * \return N/A
 *
 * \callgraph
 ******************************************************************************
 */
TemplateClass::TemplateClass
( 
    int a 
) : TemplateBaseClass() 
{
    attribute = 0;
    attributePrivate = 0;
    /* implementation */ 
    return;
}

/*
 ******************************************************************************
 * TemplateClass::~TemplateClass()
 ******************************************************************************
 *//*!
 * \brief
 * deconstructor brief
 *
 * <b> Implementation Details: </b>\n\n
 * deconstructor description
 *
 * \param - none - 
 *
 * \return N/A
 *
 * \callgraph
 ******************************************************************************
 */
TemplateClass::~TemplateClass( void )
{
    return;
}

/*
 ******************************************************************************
 * TemplateClass::publicStaticMethod()
 ******************************************************************************
 *//*!
 * \brief
 * public method brief
 *
 * <b> Implementation Details: </b>\n\n
 * public method description
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
int TemplateClass::publicStaticMethod
( 
    double a, 
    int * b, 
    char * c 
)
{
    /* implementation */
    return 0;
}

/*
 ******************************************************************************
 * TemplateClass::publicVirtualBaseMethod()
 ******************************************************************************
 *//*!
 * \brief
 * public method (override virtual method) brief
 *
 * <b> Implementation Details: </b>\n\n
 * public method (override virtual method) description 
 *
 * \param[in] a (int) input param description
 *
 * \return - none - 
 *
 * \callgraph
 ******************************************************************************
 */
void TemplateClass::publicVirtualBaseMethod
( 
    int a 
)
{
    /* implementation */
    return;
}


/*
 ******************************************************************************
 * TemplateClass::privateMethod()
 ******************************************************************************
 *//*!
 * \brief
 * private method brief
 *
 * <b> Implementation Details: </b>\n\n
 * private method description
 *
 * \param[in] a (int) input param description
 * \param[in] b (short int) modified param description
 *
 * \return (double) return value description
 *
 * \callgraph
 ******************************************************************************
 */
double TemplateClass::privateMethod
( 
    int a, 
    short int b 
)
{
    /* implementation */
    return 0.0;
}

/*-----------------------------------------------------------------------------
 * Exported Function Definitions 
 *---------------------------------------------------------------------------*/

/*-----------------------------------------------------------------------------
 * Local Function Definitions 
 *---------------------------------------------------------------------------*/

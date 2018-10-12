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
 * \file templateA.h
 * \brief header brief
 *
 * \copydetails templateA.c
 ******************************************************************************
 */

/*!
 * \defgroup templateA Template A C Module
 * @{
 * \copydoc templateA.c
 */

#ifndef TEMPLATE_A_H
#define TEMPLATE_A_H

#ifdef __cplusplus
extern "C" {
#endif

/*-----------------------------------------------------------------------------
 * Defines
 *---------------------------------------------------------------------------*/

/*! \brief exported define description */
#define TEMPLATE_EXPORT_DEF 1

/*-----------------------------------------------------------------------------
 * Macros
 *---------------------------------------------------------------------------*/

/*
 ******************************************************************************
 * TEMPLATE_EXPORT_MACRO() 
 ******************************************************************************
 *//*!
 * \brief
 * exported macro brief description
 *
 * <b> Implementation Details: </b>\n\n
 * exported macro description
 *
 * \param[in] a (double) input param description
 *
 * \callgraph
 ******************************************************************************
 */
#define TEMPLATE_EXPORT_MACRO( a )                                             \
{                                                                             \
    /* implementation */                                                      \
}

/*-----------------------------------------------------------------------------
 * Includes
 *---------------------------------------------------------------------------*/

#include <string.h>

/*-----------------------------------------------------------------------------
 * Typedefs
 *---------------------------------------------------------------------------*/

/*! \brief exported enum description */
typedef enum 
{
    /*! \brief exported enum value 1 description */
    templateExportEnum_VAL1 = 1, 
    /*! \brief exported enum value 2 description */
    templateExportEnum_VAL2 = 2 
} templateExportEnum_t;


/*! \brief exported union description */
typedef union
{
    int a;      /*!< \brief exported union element a description */
    float b;    /*!< \brief exported union element b description */
} templateExportUnion_t;


/*! \brief exported struct description */
typedef struct
{
    int a;        /*!< \brief exported struct element a description */
    void * b;     /*!< \brief exported struct element b description */
    union
    {
        double c; /*!< \brief exported struct union element c description */
        float d;  /*!< \brief exported struct union element d description */
    };
} templateExportStruct_t;    

typedef double * templateExport_t;  /*!< \brief exported typedef desc */

/*-----------------------------------------------------------------------------
 * Variables - doxygen comments in source
 *---------------------------------------------------------------------------*/

/***** see source for doxygen comments *****/
extern int templateExportVar; 

/*-----------------------------------------------------------------------------
 * Function Declarations  - doxygen comments in source
 *---------------------------------------------------------------------------*/

/***** see source for doxygen comments *****/
int templateExportFunc( double a, int * b, char * c );

/*!
 * @}
 */

#ifdef __cplusplus
}
#endif

#endif  /* TEMPLATE_H */

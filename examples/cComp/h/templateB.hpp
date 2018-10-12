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
 * \file templateB.hpp
 * \brief header brief
 *
 * \copydetails templateB.cpp
 ******************************************************************************
 */

/*!
 * \defgroup templateB Template B C++ Module
 * @{
 * \copydoc templateB.cpp
 */

#ifndef TEMPLATE_B_HPP
#define TEMPLATE_B_HPP

/*-----------------------------------------------------------------------------
 * Defines
 *---------------------------------------------------------------------------*/

/*-----------------------------------------------------------------------------
 * Macros
 *---------------------------------------------------------------------------*/

/*-----------------------------------------------------------------------------
 * Includes
 *---------------------------------------------------------------------------*/

#include "templateA.h"

/*-----------------------------------------------------------------------------
 * Typedefs
 *---------------------------------------------------------------------------*/

/*-----------------------------------------------------------------------------
 * Variables - doxygen comments in source
 *---------------------------------------------------------------------------*/

/*-----------------------------------------------------------------------------
 * Class Declarations
 *---------------------------------------------------------------------------*/

/*
 ******************************************************************************
 * TemplateBaseClass
 ******************************************************************************
 *//*!
 * \brief
 * base class brief description
 *
 * <b> Implementation Details: </b>\n\n
 * base class description
 *
 * <hr>
 * \callgraph
 ******************************************************************************
 */
class TemplateBaseClass
{
public:  

    /*-------------------------------------------------------------------------
     * Public Attributes 
     *-----------------------------------------------------------------------*/
    
    /*! \brief base class attribute description */
    int attributeBase;

    /*-------------------------------------------------------------------------
     * Public Static Attributes - doxygen comments in source 
     *-----------------------------------------------------------------------*/

    /*-------------------------------------------------------------------------
     * Public Methods - doxygen comments in source
     *-----------------------------------------------------------------------*/

    /***** see source for doxygen comments *****/
    TemplateBaseClass( void );
    virtual ~TemplateBaseClass( void );
    void publicBaseMethod( double a, int * b, char * c );
    
    /*-------------------------------------------------------------------------
     * Public Inline Methods
     *-----------------------------------------------------------------------*/

    /*-------------------------------------------------------------------------
     * Pure Virtual Methods
     *-----------------------------------------------------------------------*/

    /*
     **************************************************************************
     * TemplateBaseClass::publicVirtualBaseMethod()
     **************************************************************************
     *//*!
     * \brief
     * pure virtual method brief
     *
     * <hr>
     * <b> Implementation Details: </b>\n\n
     * pure virtual method description
     *
     * \param[in] a (int) input param description
     *
     * \return - none - 
     *
     * \callgraph
     **************************************************************************
     */
    virtual void publicVirtualBaseMethod( int a ) = 0;

protected:

    /*-------------------------------------------------------------------------
     * Protected Attributes 
     *-----------------------------------------------------------------------*/
    
    /*-------------------------------------------------------------------------
     * Protected Methods - doxygen comments in source
     *-----------------------------------------------------------------------*/

    /*-------------------------------------------------------------------------
     * Protected Inline Methods
     *-----------------------------------------------------------------------*/

private:
    
    /*-------------------------------------------------------------------------
     * Private Attributes 
     *-----------------------------------------------------------------------*/
     
    /*-------------------------------------------------------------------------
     * Private Static Attributes - doxygen comments in source 
     *-----------------------------------------------------------------------*/

    /*-------------------------------------------------------------------------
     * Private Methods - doxygen comments in source
     *-----------------------------------------------------------------------*/

    /***** see source for doxygen comments *****/
    double * privateBaseMethod( int a, short int b );

    /*-------------------------------------------------------------------------
     * Private Inline Methods
     *-----------------------------------------------------------------------*/

}; 

/*
 ******************************************************************************
 * TemplateClass
 ******************************************************************************
 *//*!
 * \brief
 * class brief description
 *
 * <b> Implementation Details: </b>\n\n
 * class description
 *
 * <hr>
 * \callgraph
 ******************************************************************************
 */
class TemplateClass : public TemplateBaseClass
{
public:  

    /*-------------------------------------------------------------------------
     * Public Attributes 
     *-----------------------------------------------------------------------*/
    
    /*! \brief class attribute variable description */
    int attribute;

    /*-------------------------------------------------------------------------
     * Public Static Attributes - doxygen comments in source 
     *-----------------------------------------------------------------------*/

    /***** see source for doxygen comments *****/
    static int attributeStatic;

    /*-------------------------------------------------------------------------
     * Public Methods - doxygen comments in source
     *-----------------------------------------------------------------------*/

    /***** see source for doxygen comments *****/
    TemplateClass( int a );
    ~TemplateClass( void );
    static int publicStaticMethod( double a, int * b, char * c );
    void publicVirtualBaseMethod( int a );

    /*-------------------------------------------------------------------------
     * Public Inline Methods
     *-----------------------------------------------------------------------*/

    /*
     **************************************************************************
     * TemplateClass::inlineMethod()
     **************************************************************************
     *//*!
     * \brief
     * inline method brief
     *
     * <hr>
     * <b> Implementation Details: </b>\n\n
     * inline method description
     *
     * \param - none -
     *
     * \return - none - 
     *
     * \callgraph
     **************************************************************************
     */
    inline void inlineMethod( void ){};

    /*-------------------------------------------------------------------------
     * Pure Virtual Methods
     *-----------------------------------------------------------------------*/

protected:

    /*-------------------------------------------------------------------------
     * Protected Attributes 
     *-----------------------------------------------------------------------*/
    
    /*-------------------------------------------------------------------------
     * Protected Methods - doxygen comments in source
     *-----------------------------------------------------------------------*/

    /*-------------------------------------------------------------------------
     * Protected Inline Methods
     *-----------------------------------------------------------------------*/

private:

    /*-------------------------------------------------------------------------
     * Private Attributes 
     *-----------------------------------------------------------------------*/
     
    /*! \brief class private attribute description */
    int attributePrivate;

    /*-------------------------------------------------------------------------
     * Private Static Attributes - doxygen comments in source 
     *-----------------------------------------------------------------------*/

    /*-------------------------------------------------------------------------
     * Private Methods - doxygen comments in source
     *-----------------------------------------------------------------------*/

    /***** see source for doxygen comments *****/
    double privateMethod( int a, short int b );

    /*-------------------------------------------------------------------------
     * Private Inline Methods
     *-----------------------------------------------------------------------*/

};

/*-----------------------------------------------------------------------------
 * Function Declarations - doxygen comments in source
 *---------------------------------------------------------------------------*/

#endif /* TEMPLATE_HPP */

/*!
 * @}
 */

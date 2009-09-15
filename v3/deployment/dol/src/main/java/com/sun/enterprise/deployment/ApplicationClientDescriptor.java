/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
 * 
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common Development
 * and Distribution License("CDDL") (collectively, the "License").  You
 * may not use this file except in compliance with the License. You can obtain
 * a copy of the License at https://glassfish.dev.java.net/public/CDDL+GPL.html
 * or glassfish/bootstrap/legal/LICENSE.txt.  See the License for the specific
 * language governing permissions and limitations under the License.
 * 
 * When distributing the software, include this License Header Notice in each
 * file and include the License file at glassfish/bootstrap/legal/LICENSE.txt.
 * Sun designates this particular file as subject to the "Classpath" exception
 * as provided by Sun in the GPL Version 2 section of the License file that
 * accompanied this code.  If applicable, add the following below the License
 * Header, with the fields enclosed by brackets [] replaced by your own
 * identifying information: "Portions Copyrighted [year]
 * [name of copyright owner]"
 * 
 * Contributor(s):
 * 
 * If you wish your version of this file to be governed by only the CDDL or
 * only the GPL Version 2, indicate your decision by adding "[Contributor]
 * elects to include this software in this distribution under the [CDDL or GPL
 * Version 2] license."  If you don't indicate a single choice of license, a
 * recipient has the option to distribute your version of this file under
 * either the CDDL, the GPL Version 2 or to extend the choice of license to
 * its licensees as provided above.  However, if you add GPL Version 2 code
 * and therefore, elected the GPL Version 2 license, then the option applies
 * only if the new code is made subject to such option by the copyright
 * holder.
 */
 package com.sun.enterprise.deployment;

import com.sun.enterprise.deployment.node.appclient.AppClientNode;
import com.sun.enterprise.deployment.runtime.JavaWebStartAccessDescriptor;
import com.sun.enterprise.deployment.types.*;
import com.sun.enterprise.deployment.util.AppClientVisitor;
import com.sun.enterprise.deployment.util.DescriptorVisitor;
import com.sun.enterprise.deployment.util.XModuleType;
import com.sun.enterprise.util.LocalStringManagerImpl;

import javax.enterprise.deploy.shared.ModuleType;
import java.util.*;
    /**
    * I represent all the deployment information about
    * an application client [{0}].
    * @author Danny Coward */

public class ApplicationClientDescriptor extends BundleDescriptor 
            implements WritableJndiNameEnvironment, 
                       ResourceReferenceContainer,
                       EjbReferenceContainer,
                       ResourceEnvReferenceContainer,
                       ServiceReferenceContainer,
                       MessageDestinationReferenceContainer
{
    private Set environmentProperties;
    private Set ejbReferences;
    private Set jmsDestReferences;
    private Set messageDestReferences;
    private Set resourceReferences;
    private Set serviceReferences;
    private Set<EntityManagerFactoryReferenceDescriptor>
        entityManagerFactoryReferences =
        new HashSet<EntityManagerFactoryReferenceDescriptor>();
    private Set<EntityManagerReferenceDescriptor>
        entityManagerReferences =
        new HashSet<EntityManagerReferenceDescriptor>();
    private Set<LifecycleCallbackDescriptor> postConstructDescs =
        new HashSet<LifecycleCallbackDescriptor>();
    private Set<LifecycleCallbackDescriptor> preDestroyDescs =
        new HashSet<LifecycleCallbackDescriptor>();
    private Set<DataSourceDefinitionDescriptor> datasourceDefinitionDescs =
            new HashSet<DataSourceDefinitionDescriptor>();
    private String mainClassName=null;
    private static LocalStringManagerImpl localStrings =
	    new LocalStringManagerImpl(ApplicationClientDescriptor.class);
    private String callbackHandler = null;
    private JavaWebStartAccessDescriptor jwsAccessDescriptor = null;
        
    /**
    * Return true if there is runtime information in this
    * object that must be saved.
    */
    public boolean hasRuntimeInformation() {
	for (Iterator itr = this.getNamedDescriptors().iterator(); itr.hasNext();) {
	    NamedDescriptor next = (NamedDescriptor) itr.next();
	    if (!"".equals(next.getJndiName())) {
		return true;
	    }
	}
	return false;
    }

    /**
     * @return the default version of the deployment descriptor
     * loaded by this descriptor
     */
    public String getDefaultSpecVersion() {
        return AppClientNode.SPEC_VERSION;
    }

    public boolean isEmpty() {
        return mainClassName==null;
    }

    /**
    * Return the fq Java clasname of this application client [{0}].
    */
    public String getMainClassName() {
	if (this.mainClassName == null) {
            this.mainClassName = "";
	}
	return this.mainClassName;
    }
    
    /**
    * Sets the main classname of this app client.
    */
    
    public void setMainClassName(String mainClassName) {
	this.mainClassName = mainClassName;
	
    }
    
    /**
     * Get the classname of the callback handler.
     */
     public String getCallbackHandler() {
	return callbackHandler;
     }

    /**
     * Set the classname of the callback handler.
     */
     public void setCallbackHandler(String handler) {
	callbackHandler = handler;

     }

    /**
    * Return the set of named descriptors I reference.
    */
    
    public Collection getNamedDescriptors() {
	return super.getNamedDescriptorsFrom(this);
    }
    
    /**
    * Return the set of named reference pairs I reference.
    */
    public Vector<NamedReferencePair> getNamedReferencePairs() {
	return super.getNamedReferencePairsFrom(this);
    }
    
    /** 
    * Returns the set of environment properties of this app client.
    */
    public Set getEnvironmentProperties() {
	if (this.environmentProperties == null) {
	    this.environmentProperties = new OrderedSet();
	}
	return this.environmentProperties = new OrderedSet(this.environmentProperties);
    }

    /** 
     * Returns the environment property object searching on the supplied key.
     * throws an illegal argument exception if no such environment property exists.
     */
    public EnvironmentProperty getEnvironmentPropertyByName(String name) {
	for (Iterator itr = this.getEnvironmentProperties().iterator(); 
             itr.hasNext();) {
	    EnvironmentProperty ev = (EnvironmentProperty) itr.next();
	    if (ev.getName().equals(name)) {
		return ev;   
	    }
	}
	throw new IllegalArgumentException(localStrings.getLocalString(
            "enterprise.deployment.exceptionappclienthasnoenvpropertybyname", 
            "This application client [{0}] has no environment property by the name of [{1}]", 
            new Object[] {getName(), name}));
    }

    
    /**
    * Adds an environment property to this application client [{0}].
    */
    
    public void addEnvironmentProperty(EnvironmentProperty environmentProperty) {
	this.getEnvironmentProperties().add(environmentProperty);

    }
    
    /**
    * Remove the given environment property
    */
    public void removeEnvironmentProperty(EnvironmentProperty environmentProperty) {
	this.getEnvironmentProperties().remove(environmentProperty);

    }
    
    /**
    * Return the set of references to ejbs that I have.
    */
    public Set getEjbReferenceDescriptors() {
	if (this.ejbReferences == null) {
	    this.ejbReferences = new OrderedSet();
	}
	return this.ejbReferences = new OrderedSet(this.ejbReferences);
    }
    
    /**
    * Add a reference to an ejb.
    */
    public void addEjbReferenceDescriptor(EjbReferenceDescriptor ejbReference) {
        addEjbReferenceDescriptor((EjbReference) ejbReference);
    }
    
    public void addEjbReferenceDescriptor(EjbReference ejbReference) {
	this.getEjbReferenceDescriptors().add(ejbReference);
        ejbReference.setReferringBundleDescriptor(this);

    }
    
    /**
    * Removes the given reference to an ejb.
    */
    public void removeEjbReferenceDescriptor(EjbReferenceDescriptor ejbReference) {
        removeEjbReferenceDescriptor((EjbReference) ejbReference);        
    }
    
    public void removeEjbReferenceDescriptor(EjbReference ejbReference) {
	this.getEjbReferenceDescriptors().remove(ejbReference);
	ejbReference.setReferringBundleDescriptor(null);

    }

    public Set<LifecycleCallbackDescriptor>
        getPostConstructDescriptors() {
        return postConstructDescs;
    }

    public void addPostConstructDescriptor(LifecycleCallbackDescriptor
        postConstructDesc) {
        String className = postConstructDesc.getLifecycleCallbackClass();
        boolean found = false;
        for (LifecycleCallbackDescriptor next :
             getPostConstructDescriptors()) {
            if ( (next.getLifecycleCallbackClass() != null) && 
                next.getLifecycleCallbackClass().equals(className)) {
                found = true;
                break;
            }
        }
        if (!found) {
            getPostConstructDescriptors().add(postConstructDesc);
        }
    }

    public LifecycleCallbackDescriptor
        getPostConstructDescriptorByClass(String className) {
        return getPostConstructDescriptorByClass(className, this);
    }

    public Set<LifecycleCallbackDescriptor> getPreDestroyDescriptors() {
        return preDestroyDescs;
    }


    public void addPreDestroyDescriptor(LifecycleCallbackDescriptor preDestroyDesc) {
        String className = preDestroyDesc.getLifecycleCallbackClass();
        boolean found = false;
        for (LifecycleCallbackDescriptor next :
             getPreDestroyDescriptors()) {
            if ( (next.getLifecycleCallbackClass() != null) && 
                next.getLifecycleCallbackClass().equals(className)) {
                found = true;
                break;
            }
        }
        if (!found) {
            getPreDestroyDescriptors().add(preDestroyDesc);
        }
    }

    public LifecycleCallbackDescriptor
        getPreDestroyDescriptorByClass(String className) {
        return getPreDestroyDescriptorByClass(className, this);
    }

    public InjectionInfo getInjectionInfoByClass(Class clazz) {
        return getInjectionInfoByClass(clazz, this);
    }

    public Set getServiceReferenceDescriptors() {
        if( this.serviceReferences == null ) {
            this.serviceReferences = new OrderedSet();
        }
        return this.serviceReferences = new OrderedSet(this.serviceReferences);
    }

    public void addServiceReferenceDescriptor(ServiceReferenceDescriptor 
                                              serviceRef) {
        serviceRef.setBundleDescriptor(this);
        this.getServiceReferenceDescriptors().add(serviceRef);

    }

    public void removeServiceReferenceDescriptor(ServiceReferenceDescriptor 
                                                 serviceRef) {
        this.getServiceReferenceDescriptors().remove(serviceRef);

    }
    
    /**
     * Looks up an service reference with the given name. 
     * Throws an IllegalArgumentException if it is not found.
     */
    public ServiceReferenceDescriptor getServiceReferenceByName(String name) {
	for (Iterator itr = this.getServiceReferenceDescriptors().iterator(); 
             itr.hasNext();) {
	    ServiceReferenceDescriptor srd = (ServiceReferenceDescriptor) 
                itr.next();
	    if (srd.getName().equals(name)) {
		return srd;
	    }
	}
	throw new IllegalArgumentException(localStrings.getLocalString(
            "enterprise.deployment.exceptionappclienthasnoservicerefbyname",
            "This application client [{0}] has no service reference by the name of [{1}]",
            new Object[] {getName(), name}));
    }

    public Set getMessageDestinationReferenceDescriptors() {
        if( this.messageDestReferences == null ) {
            this.messageDestReferences = new OrderedSet();
        }
        return this.messageDestReferences = 
            new OrderedSet(this.messageDestReferences);
    }

    public void addMessageDestinationReferenceDescriptor
        (MessageDestinationReferenceDescriptor messageDestRef) { 
        messageDestRef.setReferringBundleDescriptor(this);

        this.getMessageDestinationReferenceDescriptors().add(messageDestRef);

    }

    public void removeMessageDestinationReferenceDescriptor
        (MessageDestinationReferenceDescriptor msgDestRef) {
        this.getMessageDestinationReferenceDescriptors().remove(msgDestRef);

    }
    
    /**
     * Looks up an message destination reference with the given name. 
     * Throws an IllegalArgumentException if it is not found.
     */
    public MessageDestinationReferenceDescriptor 
        getMessageDestinationReferenceByName(String name) {
	for (Iterator itr = 
                 this.getMessageDestinationReferenceDescriptors().iterator(); 
             itr.hasNext();) {
	    MessageDestinationReferenceDescriptor mdr = 
                (MessageDestinationReferenceDescriptor) itr.next();
	    if (mdr.getName().equals(name)) {
		return mdr;
	    }
	}
	throw new IllegalArgumentException(localStrings.getLocalString(
		"exceptionappclienthasnomsgdestrefbyname", 
                "This application client [{0}] has no message destination reference by the name of [{1}]", 
                new Object[] {getName(), name}));
    }

   /**
	* Return the set of JMS destination references this ejb declares.
	*/
    public Set getJmsDestinationReferenceDescriptors() {
	if (this.jmsDestReferences == null) {
	    this.jmsDestReferences = new OrderedSet();
	}
	return this.jmsDestReferences = new OrderedSet(this.jmsDestReferences);
    }

    public void addJmsDestinationReferenceDescriptor(JmsDestinationReferenceDescriptor jmsDestReference) {
	this.getJmsDestinationReferenceDescriptors().add(jmsDestReference);

    }
    
    public void removeJmsDestinationReferenceDescriptor(JmsDestinationReferenceDescriptor jmsDestReference) {
	this.getJmsDestinationReferenceDescriptors().remove(jmsDestReference);

    }
    
    /**
    * Looks up an ejb reference with the given name. Throws an IllegalArgumentException
    * if it is not found.
    */
    public EjbReferenceDescriptor getEjbReferenceByName(String name) {
	for (Iterator itr = this.getEjbReferenceDescriptors().iterator(); itr.hasNext();) {
	    EjbReferenceDescriptor ejr = (EjbReferenceDescriptor) itr.next();
	    if (ejr.getName().equals(name)) {
		return ejr;
	    }
	}
	throw new IllegalArgumentException(localStrings.getLocalString(
            "exceptionappclienthasnoejbrefbyname", 
            "This application client [{0}] has no ejb reference by the name of [{1}]", 
            new Object[] {getName(), name}));
    }

    public Set<EntityManagerFactoryReferenceDescriptor> 
        getEntityManagerFactoryReferenceDescriptors() {
        return entityManagerFactoryReferences;
    }

    /**
     * Return the entity manager factory reference descriptor corresponding to
     * the given name.
     */
    public EntityManagerFactoryReferenceDescriptor 
        getEntityManagerFactoryReferenceByName(String name) {
        for (EntityManagerFactoryReferenceDescriptor next :
             getEntityManagerFactoryReferenceDescriptors()) {

            if (next.getName().equals(name)) {
                return next;
            }
        }
	throw new IllegalArgumentException(localStrings.getLocalString(
            "exceptionappclienthasnoentitymgrfactoryrefbyname", 
            "This application client [{0}] has no entity manager factory reference by the name of [{1}]", 
            new Object[] {getName(), name}));
    }

    public void addEntityManagerFactoryReferenceDescriptor
        (EntityManagerFactoryReferenceDescriptor reference) { 
        reference.setReferringBundleDescriptor(this);
        this.getEntityManagerFactoryReferenceDescriptors().add(reference);

    }

    public Set<EntityManagerReferenceDescriptor> 
        getEntityManagerReferenceDescriptors() {
        return entityManagerReferences;
    }

    /**
     * Return the entity manager factory reference descriptor corresponding to
     * the given name.
     */
    public EntityManagerReferenceDescriptor 
        getEntityManagerReferenceByName(String name) {
	throw new IllegalArgumentException(localStrings.getLocalString(
            "exceptionappclienthasnoentitymgrrefbyname", 
            "This application client [{0}] has no entity manager reference by the name of [{1}]", 
            new Object[] {getName(), name}));
    }

    public void addEntityManagerReferenceDescriptor
        (EntityManagerReferenceDescriptor reference) { 
        reference.setReferringBundleDescriptor(this);
        this.getEntityManagerReferenceDescriptors().add(reference);

    }

    public Set<DataSourceDefinitionDescriptor> getDataSourceDefinitionDescriptors() {
        return datasourceDefinitionDescs;
    }


    public void addDataSourceDefinitionDescriptor(DataSourceDefinitionDescriptor reference) {
        for(Iterator itr = this.getDataSourceDefinitionDescriptors().iterator(); itr.hasNext();){
            DataSourceDefinitionDescriptor desc = (DataSourceDefinitionDescriptor)itr.next();
            if(desc.getName().equals(reference.getName())){
                throw new IllegalStateException(
                        localStrings.getLocalString("exceptionappclientduplicatedatasourcedefinition",
                                "The application client [{0}] cannot have datasource definitions of same name : [{1}]",
                                getName(), reference.getName()));    
            }
        }
        getDataSourceDefinitionDescriptors().add(reference);
    }

    public void removeDataSourceDefinitionDescriptor(DataSourceDefinitionDescriptor reference) {
        this.getDataSourceDefinitionDescriptors().remove(reference);
    }

    public List<InjectionCapable> 
        getInjectableResourcesByClass(String className) {
        return getInjectableResourcesByClass(className, this);
    }
    
    /**
    * Looks up an ejb reference with the given name. Throws an IllegalArgumentException
    * if it is not found.
    */
    public EjbReference getEjbReference(String name) {
	for (Iterator itr = this.getEjbReferenceDescriptors().iterator(); itr.hasNext();) {
	    EjbReference ejr = (EjbReference) itr.next();
	    if (ejr.getName().equals(name)) {
		return ejr;
	    }
	}
	throw new IllegalArgumentException(localStrings.getLocalString(
            "exceptionappclienthasnoejbrefbyname", 
            "This application client [{0}] has no ejb reference by the name of [{1}]", 
            new Object[] {getName(), name}));
    }    

    /**
    * Return a JMS destination reference by the same name or throw an IllegalArgumentException.
    */
    public JmsDestinationReferenceDescriptor getJmsDestinationReferenceByName(String name) {
	for (Iterator itr = this.getJmsDestinationReferenceDescriptors().iterator(); itr.hasNext();) {
	    JmsDestinationReferenceDescriptor jdr = (JmsDestinationReferenceDescriptor) itr.next();
	    if (jdr.getName().equals(name)) {
		return jdr;   
	    }
	}
	throw new IllegalArgumentException(localStrings.getLocalString(
	    "enterprise.deployment.exceptionappclienthasnojmsdestrefbyname",
	    "This application client [{0}] has no resource environment reference by the name of [{1}]", 
            new Object[] {getName(), name}));
    }

    /**
    * Return the set of references to resources that I have.
    */
    public Set getResourceReferenceDescriptors() {
	if (this.resourceReferences == null) {
	    this.resourceReferences = new OrderedSet();
	}
	return this.resourceReferences = new OrderedSet(this.resourceReferences);
    }
    
    /**
    * Looks up a reference to a resource by its name (getName()). Throws an IllegalArgumentException
    * if no such descriptor is found.
    */
    public ResourceReferenceDescriptor getResourceReferenceByName(String name) {
	for (Iterator itr = this.getResourceReferenceDescriptors().iterator(); itr.hasNext();) {
	    ResourceReferenceDescriptor rr = (ResourceReferenceDescriptor) itr.next();
	    if (rr.getName().equals(name)) {
		return rr;
	    }
	}
	throw new IllegalArgumentException(localStrings.getLocalString(
	    "exceptionappclienthasnoresourcerefbyname",
	    "This application client [{0}] has no resource reference by the name of [{1}]", 
            new Object[] {getName(), name}));
    }
    
    /**
    * Adds a reference to a resource.
    */
    
    public void addResourceReferenceDescriptor(ResourceReferenceDescriptor resourceReference) {
	this.getResourceReferenceDescriptors().add(resourceReference);

    }
    
    /** 
    * Removes the given resource reference from this app client.
    */
    public void removeResourceReferenceDescriptor(ResourceReferenceDescriptor resourceReference) {
	this.getResourceReferenceDescriptors().remove(resourceReference);

    }

    /**
     * @return a set of class names that need to have full annotation processing
     */
    public Set<String> getComponentClassNames() {
        Set set = new HashSet<String>();
        set.add(getMainClassName());
        return set;
    }

    
    /**
     * @return true if this bundle descriptor defines web service clients
     */
    public boolean hasWebServiceClients() {
        return !(getServiceReferenceDescriptors().isEmpty());
    } 
    
    /**
     * @return true if this bundle descriptor defines web services
     */
    public boolean hasWebServices() {
        return false;
    }    
    
    public void print(StringBuffer toStringBuffer) {
        toStringBuffer.append("Application Client Descriptor");
	toStringBuffer.append("\n ");
        super.print(toStringBuffer);
	toStringBuffer.append("\n environmentProperties ").append(environmentProperties);
	toStringBuffer.append("\n ejbReferences ");
        if(ejbReferences != null)
            printDescriptorSet(ejbReferences,toStringBuffer);
        toStringBuffer.append("\n jmsDestReferences ");
        if(jmsDestReferences != null)
            printDescriptorSet(jmsDestReferences,toStringBuffer);
        toStringBuffer.append("\n messageDestReferences ");
        if(messageDestReferences != null)
            printDescriptorSet(messageDestReferences,toStringBuffer);
	toStringBuffer.append("\n resourceReferences ");
        if(resourceReferences != null)
            printDescriptorSet(resourceReferences,toStringBuffer);
        toStringBuffer.append("\n serviceReferences ");
        if(serviceReferences != null)
            printDescriptorSet(serviceReferences,toStringBuffer);
	toStringBuffer.append("\n mainClassName ").append(mainClassName);
    }
    private void printDescriptorSet(Set descSet, StringBuffer sbuf){
        for(Iterator itr = descSet.iterator(); itr.hasNext();){
            Object obj = itr.next();
            if(obj instanceof Descriptor)
                ((Descriptor)obj).print(sbuf);
            else
                sbuf.append(obj);
        }
    }

    /** 
     * visit the descriptor and all sub descriptors with a DOL visitor implementation
     * 
     * @param aVisitor a visitor to traverse the descriptors
     */    
    public void visit(DescriptorVisitor aVisitor) {
        if (aVisitor instanceof AppClientVisitor) {
            visit((AppClientVisitor) aVisitor);
        } else {
            super.visit(aVisitor);
        }
    }    
    
     /** 
     * visit the descriptor and all sub descriptors with a DOL visitor implementation
     * 
     * @param aVisitor a visitor to traverse the descriptors
     */
    public void visit(AppClientVisitor aVisitor) {
        aVisitor.accept(this);

        // Visit all injectables first.  In some cases, basic type information
        // has to be derived from target inject method or inject field.
        for(InjectionCapable injectable : getInjectableResources(this)) {
            aVisitor.accept(injectable);
        }

        Set ejbRefs = getEjbReferenceDescriptors();
        for (Iterator itr = ejbRefs.iterator();itr.hasNext();) {
            aVisitor.accept((EjbReference) itr.next());
        }

        for (Iterator itr=getResourceReferenceDescriptors().iterator();
             itr.hasNext();) {
            ResourceReferenceDescriptor next = 
                (ResourceReferenceDescriptor) itr.next();
            aVisitor.accept(next);
        }

        for (Iterator itr=getJmsDestinationReferenceDescriptors().iterator();
             itr.hasNext();) {
            JmsDestinationReferenceDescriptor next = 
                (JmsDestinationReferenceDescriptor) itr.next();
            aVisitor.accept(next);
        }

        Set msgDestRefs = getMessageDestinationReferenceDescriptors(); 
        for (Iterator itr = msgDestRefs.iterator();itr.hasNext();) {
            aVisitor.accept((MessageDestinationReferencer) itr.next());
        }

        for (Iterator itr = getMessageDestinations().iterator();
                itr.hasNext();) {
            MessageDestinationDescriptor msgDestDescriptor =
                (MessageDestinationDescriptor)itr.next();
            aVisitor.accept(msgDestDescriptor);
        }

        Set serviceRefs = getServiceReferenceDescriptors();
        for (Iterator itr = serviceRefs.iterator();itr.hasNext();) {
            aVisitor.accept((ServiceReferenceDescriptor) itr.next());
        }
    }
    
    /**
     * @return the module type for this bundle descriptor
     */
    public XModuleType getModuleType() {
        return XModuleType.CAR;
    }
    
    public JavaWebStartAccessDescriptor getJavaWebStartAccessDescriptor() {
        if (jwsAccessDescriptor == null) {
            jwsAccessDescriptor = new JavaWebStartAccessDescriptor();
            jwsAccessDescriptor.setBundleDescriptor(this);
        }
        return jwsAccessDescriptor;
    }
    
    public void setJavaWebStartAccessDescriptor(JavaWebStartAccessDescriptor descr) {
        descr.setBundleDescriptor(this);
        jwsAccessDescriptor = descr;

    }

    /**
     * This method is used to find out the precise list of PUs that are
     * referenced by the appclient. An appclient can not use container
     * managed EM as there is no support for JTA in our ACC, so this method
     * only returns the list of PUs referenced via @PersistenceUnit or
     * <persistence-unit-ref>.
     *
     * @return list of PU that are actually referenced by the appclient.
     */
    public Collection<? extends PersistenceUnitDescriptor> findReferencedPUs() {
        return findReferencedPUsViaPURefs(this);
    }
}

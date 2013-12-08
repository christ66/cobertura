/*
 * #%L
 * cobertura-metrics-model
 * %%
 * Copyright (C) 2013 Cobertura
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */
package net.sourceforge.cobertura.metrics.model.location;

import net.sourceforge.cobertura.metrics.model.Namespace;
import org.apache.commons.lang3.Validate;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;
import java.io.Serializable;
import java.lang.reflect.Method;

/**
 * Simple Source code Location definition, refraining from using java reflection
 * types to reduce class loading requirements.
 *
 * @author <a href="mailto:lj@jguru.se">Lennart J&ouml;relid</a>, jGuru Europe AB
 */
@XmlType(namespace = Namespace.COBERTURA_NAMESPACE,
        propOrder = {"packageName", "className", "methodName", "lineNumber"})
@XmlAccessorType(XmlAccessType.FIELD)
public class SourceLocation implements Serializable, Comparable<SourceLocation> {

    private static final long serialVersionUID = 0xC0BE001;

    // Internal state
    @XmlAttribute(required = true)
    private String packageName;

    @XmlElement(required = true, nillable = false)
    private String className;

    @XmlElement(required = true, nillable = false)
    private String methodName;

    @XmlAttribute(required = false)
    private int lineNumber;

    /**
     * JAXB-friendly constructor.
     */
    public SourceLocation() {
    }

    /**
     * Compound constructor, wrapping the supplied data.
     *
     * @param packageName The name of the package containing this SourceLocation, corresponding
     *                    to {@code Package.getName() }.
     * @param className   The simple name of the class containing this SourceLocation,
     *                    corresponding to {@code Class.getSimpleName() }.
     * @param methodName  The name of the method containing this SourceLocation, corresponding to
     *                    {@code Method.getName() }.
     * @param lineNumber  The line number in the class containing this SourceLocation.
     */
    public SourceLocation(final String packageName,
                          final String className,
                          final String methodName,
                          final int lineNumber) {

        // Check sanity
        Validate.notEmpty(packageName, "Cannot handle null or empty packageName argument.");
        Validate.notEmpty(className, "Cannot handle null or empty className argument.");
        Validate.notEmpty(methodName, "Cannot handle null or empty methodName argument.");
        Validate.isTrue(lineNumber >= 0, "Cannot handle negative lineNumber argument.");

        // Assign internal state
        this.packageName = packageName;
        this.className = className;
        this.methodName = methodName;
        this.lineNumber = lineNumber;
    }

    /**
     * Convenience constructor digging out package class and method names from
     * the supplied Method object.
     *
     * @param method     The non-null Method object holding package, class and method names.
     * @param lineNumber The line number in the class containing this SourceLocation.
     */
    public SourceLocation(final Method method,
                          final int lineNumber) {
        // Delegate
        this(method.getDeclaringClass().getPackage().getName(),
                method.getDeclaringClass().getSimpleName(),
                method.getName(),
                lineNumber);
    }

    /**
     * @return The name of the package containing this SourceLocation, corresponding
     * to {@code Package.getName() }.
     */
    public String getPackageName() {
        return packageName;
    }

    /**
     * @return The simple name of the class containing this SourceLocation,
     *                    corresponding to {@code Class.getSimpleName() }.
     */
    public String getClassName() {
        return className;
    }

    /**
     * @return The name of the method containing this SourceLocation, corresponding to
     *                    {@code Method.getName() }.
     */
    public String getMethodName() {
        return methodName;
    }

    /**
     * @return The line number in the class containing this SourceLocation.
     */
    public int getLineNumber() {
        return lineNumber;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int compareTo(final SourceLocation that) {

        // Check sanity
        if (that == null) {
            return Integer.MIN_VALUE;
        }
        if (that == this) {
            return 0;
        }

        // All done.
        return toString().compareTo(that.toString());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        return getPackageName() + "." + getClassName() + "::" + getMethodName() + ",line:" + getLineNumber();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int hashCode() {
        return getPackageName().hashCode()
                + getClassName().hashCode()
                + getMethodName().hashCode()
                + getLineNumber();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean equals(Object obj) {

        // Check sanity
        if(obj == null) {
            return false;
        }

        // All done.
        return (obj instanceof SourceLocation) && (hashCode() == obj.hashCode());
    }
}

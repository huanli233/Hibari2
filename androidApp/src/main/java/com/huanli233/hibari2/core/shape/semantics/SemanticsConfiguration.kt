/*
 * Copyright 2019 The Android Open Source Project
 *
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
 */

package androidx.compose.ui.semantics

import androidx.collection.MutableScatterMap
import androidx.collection.MutableScatterSet
import androidx.collection.ScatterSet
import androidx.collection.mutableScatterMapOf
import androidx.collection.mutableScatterSetOf
import androidx.compose.ui.platform.simpleIdentityToString

/**
 * Describes the semantic information associated with the owning component
 *
 * The information provided in the configuration is used to to generate the semantics tree.
 */
class SemanticsConfiguration :
    com.huanli233.hibari2.core.semantics.SemanticsPropertyReceiver, Iterable<Map.Entry<com.huanli233.hibari2.core.semantics.SemanticsPropertyKey<*>, Any?>> {

    internal val props: MutableScatterMap<com.huanli233.hibari2.core.semantics.SemanticsPropertyKey<*>, Any?> = mutableScatterMapOf()
    private var mapWrapper: Map<com.huanli233.hibari2.core.semantics.SemanticsPropertyKey<*>, Any?>? = null

    private var _accessibilityExtraKeys: MutableScatterSet<com.huanli233.hibari2.core.semantics.SemanticsPropertyKey<*>>? = null
    internal val accessibilityExtraKeys: ScatterSet<com.huanli233.hibari2.core.semantics.SemanticsPropertyKey<*>>?
        get() = _accessibilityExtraKeys

    /**
     * Retrieves the value for the given property, if one has been set. If a value has not been set,
     * throws [IllegalStateException]
     */
    // Unavoidable, guaranteed by [set]
    @Suppress("UNCHECKED_CAST")
    operator fun <T> get(key: com.huanli233.hibari2.core.semantics.SemanticsPropertyKey<T>): T {
        return props.getOrElse(key) {
            throw IllegalStateException("Key not present: $key - consider getOrElse or getOrNull")
        } as T
    }

    // Unavoidable, guaranteed by [set]
    @Suppress("UNCHECKED_CAST")
    fun <T> getOrElse(key: com.huanli233.hibari2.core.semantics.SemanticsPropertyKey<T>, defaultValue: () -> T): T {
        return props.getOrElse(key, defaultValue) as T
    }

    // Unavoidable, guaranteed by [set]
    @Suppress("UNCHECKED_CAST")
    fun <T> getOrElseNullable(key: com.huanli233.hibari2.core.semantics.SemanticsPropertyKey<T>, defaultValue: () -> T?): T? {
        return props.getOrElse(key, defaultValue) as T?
    }

    override fun iterator(): Iterator<Map.Entry<com.huanli233.hibari2.core.semantics.SemanticsPropertyKey<*>, Any?>> {
        @Suppress("AsCollectionCall")
        val mapWrapper = mapWrapper ?: props.asMap().apply { mapWrapper = this }
        return mapWrapper.iterator()
    }

    override fun <T> set(key: com.huanli233.hibari2.core.semantics.SemanticsPropertyKey<T>, value: T) {
        if (value is com.huanli233.hibari2.core.semantics.AccessibilityAction<*> && contains(key)) {
            val prev = props[key] as com.huanli233.hibari2.core.semantics.AccessibilityAction<*>
            props[key] =
                _root_ide_package_.com.huanli233.hibari2.core.semantics.AccessibilityAction(
                    value.label ?: prev.label, value.action ?: prev.action
                )
        } else {
            props[key] = value
        }

        if (key.accessibilityExtraKey != null) {
            if (_accessibilityExtraKeys == null) _accessibilityExtraKeys = mutableScatterSetOf()
            _accessibilityExtraKeys?.add(key)
        }
    }

    operator fun <T> contains(key: com.huanli233.hibari2.core.semantics.SemanticsPropertyKey<T>): Boolean {
        return props.containsKey(key)
    }

    internal fun containsImportantForAccessibility() =
        props.any { key, _ -> key.isImportantForAccessibility }

    /**
     * Whether the semantic information provided by the owning component and all of its descendants
     * should be treated as one logical entity.
     *
     * If set to true, the descendants of the owning component's [com.huanli233.hibari2.core.semantics.SemanticsNode] will merge their
     * semantic information into the [com.huanli233.hibari2.core.semantics.SemanticsNode] representing the owning component.
     */
    var isMergingSemanticsOfDescendants: Boolean = false
    var isClearingSemantics: Boolean = false

    // CONFIGURATION COMBINATION LOGIC

    /**
     * Absorb the semantic information from a child SemanticsNode into this configuration.
     *
     * This merges the child's semantic configuration using the `merge()` method defined on the key.
     * This is used when mergeDescendants is specified (for accessibility focusable nodes).
     */
    @Suppress("UNCHECKED_CAST")
    internal fun mergeChild(child: com.huanli233.hibari2.core.semantics.SemanticsConfiguration) {
        child.props.forEach { key, nextValue ->
            val existingValue = props[key]
            val mergeResult = (key as com.huanli233.hibari2.core.semantics.SemanticsPropertyKey<Any?>).merge(existingValue, nextValue)
            if (mergeResult != null) {
                props[key] = mergeResult
            }
        }
    }

    /**
     * Absorb the semantic information from a peer modifier into this configuration.
     *
     * This is repeatedly called for each semantics {} modifier on one LayoutNode to collapse them
     * into one SemanticsConfiguration. If a key is already seen and the value is
     * AccessibilityAction, the resulting AccessibilityAction's label/action will be the
     * label/action of the outermost modifier with this key and nonnull label/action, or null if no
     * nonnull label/action is found. If the value is not AccessibilityAction, values with a key
     * already seen are ignored (the semantics value of the outermost modifier with a given
     * semantics key is the one used).
     */
    internal fun collapsePeer(peer: com.huanli233.hibari2.core.semantics.SemanticsConfiguration) {
        if (peer.isMergingSemanticsOfDescendants) {
            isMergingSemanticsOfDescendants = true
        }
        if (peer.isClearingSemantics) {
            isClearingSemantics = true
        }
        peer.props.forEach { key, nextValue ->
            if (!props.contains(key)) {
                props[key] = nextValue
            } else if (nextValue is com.huanli233.hibari2.core.semantics.AccessibilityAction<*>) {
                val value = props[key] as com.huanli233.hibari2.core.semantics.AccessibilityAction<*>
                props[key] =
                    _root_ide_package_.com.huanli233.hibari2.core.semantics.AccessibilityAction(
                        value.label ?: nextValue.label,
                        value.action ?: nextValue.action,
                    )
            }
        }
    }

    /** Returns an exact copy of this configuration. */
    fun copy(): com.huanli233.hibari2.core.semantics.SemanticsConfiguration {
        val copy = com.huanli233.hibari2.core.semantics.SemanticsConfiguration()
        copy.isMergingSemanticsOfDescendants = isMergingSemanticsOfDescendants
        copy.isClearingSemantics = isClearingSemantics
        copy.props.putAll(props)
        return copy
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is com.huanli233.hibari2.core.semantics.SemanticsConfiguration) return false

        if (props != other.props) return false
        if (isMergingSemanticsOfDescendants != other.isMergingSemanticsOfDescendants) return false
        if (isClearingSemantics != other.isClearingSemantics) return false

        return true
    }

    override fun hashCode(): Int {
        var result = props.hashCode()
        result = 31 * result + isMergingSemanticsOfDescendants.hashCode()
        result = 31 * result + isClearingSemantics.hashCode()
        return result
    }

    override fun toString(): String {
        val propsString = StringBuilder()
        var nextSeparator = ""

        if (isMergingSemanticsOfDescendants) {
            propsString.append(nextSeparator)
            propsString.append("mergeDescendants=true")
            nextSeparator = ", "
        }

        if (isClearingSemantics) {
            propsString.append(nextSeparator)
            propsString.append("isClearingSemantics=true")
            nextSeparator = ", "
        }

        props.forEach { key, value ->
            propsString.append(nextSeparator)
            propsString.append(key.name)
            propsString.append(" : ")
            propsString.append(value)
            nextSeparator = ", "
        }
        return "${simpleIdentityToString(this@SemanticsConfiguration, null)}{ $propsString }"
    }
}

fun <T> com.huanli233.hibari2.core.semantics.SemanticsConfiguration.getOrNull(key: com.huanli233.hibari2.core.semantics.SemanticsPropertyKey<T>): T? {
    return getOrElseNullable(key) { null }
}

(ns hara.reflect.element.method
  (:require [hara.reflect.common :refer :all]
            [hara.reflect.hierarchy :as hierarchy]
            [hara.reflect.types.element :refer :all]
            [hara.reflect.element.common :refer :all]
            [hara.reflect.pretty.classes :refer [class-convert]]))

(defn invoke-static-method
  ([ele]
     (try (.invoke ^java.lang.reflect.Method (:delegate ele) nil (object-array []))
          (catch IllegalArgumentException e
            (throw-arg-exception ele []))))
  ([ele args]
     (.invoke ^java.lang.reflect.Method (:delegate ele) nil (object-array (box-args ele args)))))

(defn invoke-instance-method [ele args]
  (let [bargs (box-args ele args)]
    (.invoke ^java.lang.reflect.Method (:delegate ele) (first bargs) (object-array (rest bargs)))))

(defmethod invoke-element :method
  ([ele]
     (if (:static ele)
       (invoke-static-method ele)
       (throw-arg-exception ele [])))
  ([ele & args]
     (if (:static ele)
       (invoke-static-method ele args)
       (invoke-instance-method ele args))))

(defn to-static-method [^java.lang.reflect.Method obj body]
  (-> body
      (assoc :params (vec (seq (.getParameterTypes obj))))
      (assoc :origins (list (.getDeclaringClass obj)))))

(defn to-instance-method
  [^java.lang.reflect.Method obj body]
  (-> body
      (assoc :params (vec (cons (:container body) (seq (.getParameterTypes obj)))))
      (assoc :origins (hierarchy/origins obj))))

(defn to-pre-element [obj]
  (let [body (seed :method obj)
        body (if (:static body)
               (to-static-method obj body)
               (to-instance-method obj body))]
    body))

(defmethod to-element java.lang.reflect.Method
  [^java.lang.reflect.Method obj]
  (let [body (-> (to-pre-element obj)
                 (assoc :type (.getReturnType obj)))]
    (element body)))

(defmethod format-element :method [ele]
  (format-element-method ele))

(defmethod element-params :method [ele]
  (list (element-params-method ele)))

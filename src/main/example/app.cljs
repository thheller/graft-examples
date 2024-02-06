(ns example.app
  (:require
    [shadow.cljs.modern :refer (js-await)]
    [shadow.graft :as graft]))

(defn data-get [el key]
  (unchecked-get (.-dataset el) (name key)))

(defonce editing-ref (atom nil))

(defn cancel-edit [opts]
  (when-some [{:keys [node pos]} @editing-ref]
    (.insertAdjacentElement pos "beforebegin" node)
    (.remove pos)
    (reset! editing-ref nil)
    ))

(defn edit-row [opts tr]
  (if @editing-ref
    (when (js/confirm "already editing, edit this instead?")
      (cancel-edit opts)
      (edit-row opts tr))

    (let [id (data-get tr :id)]
      ;; no actual server, so using the same edit doc for all
      (js-await [res (js/fetch (str (get opts "type") "--edit.html?id=" id))]
        (js-await [body (.text res)]
          (.insertAdjacentHTML tr "afterend" body)
          (reset! editing-ref {:node tr :pos (.-nextElementSibling tr)})
          (.remove tr)
          )))))

(defn submit-edit [opts tr]
  (js/console.log "exercise left to the reader!"))

(defmethod graft/scion "edi-table" [opts tbody]
  (.addEventListener tbody "click"
    (fn [e]
      (when-some [el (.closest (.-target e) "[data-action]")]
        (let [tr (.closest el "tr")]
          (case (data-get el :action)
            "edit"
            (edit-row opts tr)
            "cancel"
            (cancel-edit opts)
            "submit"
            (submit-edit opts tr)
            ))))))

(defn init []
  (graft/init (comp js->clj js/JSON.parse)))

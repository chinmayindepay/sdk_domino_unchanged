package com.indepay.umps.pspsdk.beneficiary.Contacts

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.Animation.RELATIVE_TO_SELF
import android.view.animation.RotateAnimation
import android.widget.Filter
import android.widget.Filterable
import com.indepay.umps.pspsdk.R
import com.indepay.umps.pspsdk.callbacks.OnContactListInteractionListener
import com.indepay.umps.pspsdk.models.Communication
import com.indepay.umps.pspsdk.models.Contact
import com.thoughtbot.expandablerecyclerview.ExpandableRecyclerViewAdapter
import com.thoughtbot.expandablerecyclerview.listeners.GroupExpandCollapseListener
import com.thoughtbot.expandablerecyclerview.models.ExpandableGroup
import com.thoughtbot.expandablerecyclerview.viewholders.ChildViewHolder
import com.thoughtbot.expandablerecyclerview.viewholders.GroupViewHolder
import kotlinx.android.synthetic.main.list_item_contact_number.view.*
import kotlinx.android.synthetic.main.list_item_display_name.view.*


class ContactListAdapter(
        private val context: Context,
        private val contactList: MutableList<Contact>,
        private val listenerContact: OnContactListInteractionListener?,
        private val expandCallback: ExpandListener)
    : ExpandableRecyclerViewAdapter<ContactListAdapter.DisplayNameViewHolder,
        ContactListAdapter.ContactNumberViewHolder>(contactList), Filterable {
    private val contactFilter = ContactFilter()
    private val tempContactList = ArrayList<Contact>()

    init {
        tempContactList.addAll(contactList)
    }

    override fun onCreateGroupViewHolder(parent: ViewGroup?, viewType: Int): DisplayNameViewHolder {
        val view = LayoutInflater.from(parent?.context).inflate(R.layout.list_item_display_name, parent, false)
        return DisplayNameViewHolder(view)
    }

    override fun onBindGroupViewHolder(holder: DisplayNameViewHolder?, flatPosition: Int, group: ExpandableGroup<*>?) {
        val materialColors = context.resources.getIntArray(R.array.colors)
        val contact = group as Contact
        holder?.letterIcon?.letter = contact.displayName
        holder?.letterIcon?.shapeColor = materialColors[flatPosition % materialColors.size]
        holder?.diaplayName?.text = contact.displayName
    }

    override fun onCreateChildViewHolder(parent: ViewGroup?, viewType: Int): ContactNumberViewHolder {
        val view = LayoutInflater.from(parent?.context).inflate(R.layout.list_item_contact_number, parent, false)
        val contactNoViewHolder = ContactNumberViewHolder(view)
        contactNoViewHolder.contactView.setOnClickListener {
            listenerContact?.onContactListInteraction(contactNoViewHolder.contactNumber.text.toString().replace("\\s".toRegex(), ""),"anmol")
        }
        return contactNoViewHolder
    }

    override fun onBindChildViewHolder(holder: ContactNumberViewHolder?, flatPosition: Int, group: ExpandableGroup<*>?, childIndex: Int) {
        val communication = group?.items?.get(childIndex) as Communication
        holder?.contactNumber?.text = communication.phoneNumber
    }


    override fun onGroupExpanded(positionStart: Int, itemCount: Int) {
        super.onGroupExpanded(positionStart, itemCount)
        var counter: Int
        for (index in 0 until groups.size) {
            if (isGroupExpanded(groups[index])) {
                counter = groups[index].itemCount
                val groupIndex = expandableList.getUnflattenedPosition(positionStart).groupPos
                if (groupIndex == groups.size - 1) {
                    expandCallback.onExpand(positionStart + counter)
                }
            }
        }
    }
    override fun getFilter(): Filter {
        return contactFilter
    }

    inner class DisplayNameViewHolder(nameView: View) : GroupViewHolder(nameView) {
        val letterIcon = nameView.letter_icon
        val diaplayName = nameView.display_name
        val expandArrow = nameView.arrow_down

        override fun expand() {
            super.expand()
            val rotate = RotateAnimation(360f, 180f, RELATIVE_TO_SELF,
                    0.5f, RELATIVE_TO_SELF, 0.5f)
            rotate.duration = 300
            rotate.fillAfter = true
            expandArrow.animation = rotate
        }

        override fun collapse() {
            super.collapse()
            val rotate = RotateAnimation(180f, 360f, RELATIVE_TO_SELF,
                    0.5f, RELATIVE_TO_SELF, 0.5f)
            rotate.duration = 300
            rotate.fillAfter = true
            expandArrow.animation = rotate
        }
    }

    inner class ContactNumberViewHolder(val contactView: View) : ChildViewHolder(contactView) {
        val contactNumber = contactView.contact_no
    }

    inner class ContactFilter : Filter() {

        override fun performFiltering(constraint: CharSequence?): FilterResults {

            return if (constraint != null) {
                contactList.clear()
                val query = constraint.toString().toLowerCase()
                if (!query.isEmpty()) {
                    for (contact in tempContactList) {
                        val communication = Communication(query)
                        if (contact.displayName.orEmpty().toLowerCase().contains(query) ||
                                contact.communications.contains(communication))
                            contactList.add(contact)
                    }
                }

                val filterResults = Filter.FilterResults()
                filterResults.count = contactList.size
                filterResults
            } else {
                FilterResults()
            }
            notifyDataSetChanged()
        }

        override fun publishResults(charSequence: CharSequence?, results: FilterResults?) {
            if (results != null) {
                if (results.count <= 0 && charSequence.isNullOrEmpty()) {
                    this@ContactListAdapter.contactList.addAll(tempContactList)
                }
            }
            notifyDataSetChanged()
        }

    }

    interface ExpandListener {
        fun onExpand(position: Int)
    }
}

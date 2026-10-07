package com.localdeals.finder.data.mock

import com.localdeals.finder.domain.model.Category
import com.localdeals.finder.domain.model.Deal
import com.localdeals.finder.domain.model.Seller
import java.util.UUID

object MockData {
    val categories = listOf(
        Category("cat_1", "Food", "fastfood"),
        Category("cat_2", "Grocery", "shopping_cart"),
        Category("cat_3", "Fashion", "checkroom"),
        Category("cat_4", "Electronics", "devices"),
        Category("cat_5", "Beauty", "face"),
        Category("cat_6", "Health", "favorite"),
        Category("cat_7", "Services", "build"),
        Category("cat_8", "Home", "home")
    )

    val sellers = listOf(
        Seller("s_1", "user_s1", "Bhai Bhai Super Shop", "Mirpur 10, Dhaka", 23.8223, 90.3654, 4.5, "gradient_1"),
        Seller("s_2", "user_s2", "Dhaka Electronics", "Multiplan Center, Elephant Road", 23.7386, 90.3882, 4.8, "gradient_2"),
        Seller("s_3", "user_s3", "Fashion Plus", "Bashundhara City, Panthapath", 23.7497, 90.3900, 4.2, "gradient_3"),
        Seller("s_4", "user_s4", "Fresh Bites", "Banani, Block C", 23.7940, 90.4043, 4.7, "gradient_4"),
        Seller("s_5", "user_s5", "Daily Grocery", "Gulshan 1, Dhaka", 23.7806, 90.4162, 4.1, "gradient_5"),
        Seller("s_6", "user_s6", "Gadget Hub", "Jamuna Future Park, Kuril", 23.8136, 90.4243, 4.9, "gradient_6"),
        Seller("s_7", "user_s7", "Aesthetic Beauty", "Dhanmondi 27", 23.7548, 90.3755, 4.4, "gradient_7"),
        Seller("s_8", "user_s8", "Healthy Life Pharmacy", "Uttara Sector 11", 23.8759, 90.3938, 4.6, "gradient_8"),
        Seller("s_9", "user_s9", "Quick Fix Services", "Motijheel, Dhaka", 23.7330, 90.4172, 4.0, "gradient_9"),
        Seller("s_10", "user_s10", "Home Decor BD", "Badda, Dhaka", 23.7805, 90.4267, 4.3, "gradient_10"),
        Seller("s_11", "user_s11", "Mayer Doa Hotel", "Mohakhali, Dhaka", 23.7779, 90.3999, 3.9, "gradient_11"),
        Seller("s_12", "user_s12", "Sneaker House", "Khilgaon, Dhaka", 23.7460, 90.4253, 4.5, "gradient_12")
    )

    private val titles = listOf(
        "50% Off Burger", "Fresh Apples 1kg", "Summer T-Shirt", "Wireless Mouse", "Facial Kit",
        "Vitamin C Tablets", "AC Servicing", "Bedsheet Set", "Fried Rice Combo", "Milk 1L",
        "Jeans Pant", "Mechanical Keyboard", "Shampoo", "Blood Pressure Monitor", "Plumbing Service",
        "Curtains", "Biryani Pack", "Onion 2kg", "Winter Jacket", "Bluetooth Headphones",
        "Hair Oil", "First Aid Kit", "Car Wash", "Table Lamp", "Pizza Large", "Eggs 1 Dozen",
        "Formal Shirt", "Smart Watch", "Body Lotion", "Thermometer", "House Cleaning", "Wall Clock",
        "Coffee Beans", "Rice 5kg", "Sneakers", "Power Bank", "Perfume", "Pain Relief Spray",
        "Laundry Service", "Cushion Covers"
    )

    val deals = buildList {
        val now = System.currentTimeMillis()
        val oneHour = 60 * 60 * 1000L
        val oneDay = 24 * oneHour

        for (i in 0 until 40) {
            val seller = sellers[i % sellers.size]
            val category = categories[i % categories.size]
            val original = 500.0 + (i * 50)
            val discount = 10 + (i % 50) // 10% to 59%
            val discounted = original * (1 - (discount / 100.0))
            
            // Some deals expire soon (within 2 hours), some later
            val expiresAt = if (i % 5 == 0) now + (oneHour * (1 + (i % 2))) else now + (oneDay * (1 + (i % 5)))
            
            add(
                Deal(
                    id = "d_$i",
                    sellerId = seller.id,
                    title = titles[i % titles.size],
                    description = "Amazing deal on ${titles[i % titles.size]}. Hurry up before stock runs out! Exclusive offer available only at ${seller.businessName}.",
                    originalPrice = original,
                    discountedPrice = discounted,
                    discountPercentage = discount,
                    stock = 5 + (i % 15),
                    categoryId = category.id,
                    expirationTimeMillis = expiresAt,
                    imageUrl = "gradient_${(i % 12) + 1}"
                )
            )
        }
    }
}
